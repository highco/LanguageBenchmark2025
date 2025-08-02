import kotlin.system.measureTimeMillis

data class User(
    val userId: Int,
    var inputs: ByteArray = ByteArray(128) // Pre-allocate with capacity
) {
    var inputsSize: Int = 0 // Track actual size to avoid ArrayList overhead
    
    // Fast append operation using pre-allocated array
    inline fun addInputs(newInputs: ByteArray) {
        val requiredSize = inputsSize + newInputs.size
        
        // Expand array if needed with growth factor
        if (requiredSize > inputs.size) {
            val newCapacity = maxOf(requiredSize, inputs.size * 2)
            val newArray = ByteArray(newCapacity)
            System.arraycopy(inputs, 0, newArray, 0, inputsSize)
            inputs = newArray
        }
        
        // Copy new inputs directly
        System.arraycopy(newInputs, 0, inputs, inputsSize, newInputs.size)
        inputsSize += newInputs.size
    }
}

data class Room(
    val roomId: Int,
    val users: HashMap<Int, User> = HashMap(32) // Pre-allocate for expected users
)

// Use HashMap for O(1) room lookup - pre-allocate for expected rooms
private val rooms = HashMap<Int, Room>(32)

// Pre-allocated sample inputs to avoid repeated array creation
private val sampleInputs = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

fun main() {
    benchmark()
}
private fun benchmark() {
    val timeMs = measureTimeMillis {
        // First benchmark: room joining
        repeat(50_000_000) { i ->
            joinRoom(i % 17, i % 997)
        }
        
        // Second benchmark: adding inputs
        repeat(50_000_000) { i ->
            addUserInputs(i % 17, i % 997, sampleInputs)
        }
    }
    
    println("${timeMs}ms")
}

// Optimized room joining with minimal allocations
private inline fun joinRoom(roomId: Int, userId: Int) {
    // Get or create room - O(1) operation
    val room = rooms.getOrPut(roomId) {
        Room(roomId, HashMap(32))
    }
    
    // Add user if not exists - O(1) operation
    room.users.putIfAbsent(userId, User(userId))
}

// Optimized input addition with direct array manipulation
private inline fun addUserInputs(roomId: Int, userId: Int, inputs: ByteArray) {
    // O(1) room lookup
    val room = rooms[roomId] ?: return
    
    // O(1) user lookup and fast input addition
    room.users[userId]?.addInputs(inputs)
}