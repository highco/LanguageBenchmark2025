import kotlin.system.measureTimeMillis

data class User(
    val userId: Int,
    var inputs: ByteArray = ByteArray(64) // Pre-allocate with capacity
) {
    var inputsSize: Int = 0 // Track actual size to avoid ArrayList overhead
    
    // Fast append operation using pre-allocated array
    fun addInputs(newInputs: ByteArray) {
        val requiredSize = inputsSize + newInputs.size
        
        // Expand array if needed with growth factor
        if (requiredSize > inputs.size) {
            val newCapacity = requiredSize*2
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
    val users: HashMap<Int, User> = HashMap(64) // Pre-allocate for expected users
)

// Use HashMap for O(1) room lookup - pre-allocate for expected rooms
private val rooms = HashMap<Int, Room>(64)

// Pre-allocated sample inputs to avoid repeated array creation
private val sampleInputs = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

fun main() {
    benchmark()
}
private fun benchmark() {
    val timeMs = measureTimeMillis {
        for(room_id in 0 until 977) {
            for(user_id in 0 until 173) {
                joinRoom(room_id, user_id);
            }
        }
        
        // Second loop: add user inputs
        for (i in 0 until 123_456_789) {
            addUserInputs(i % 977, i % 173, sampleInputs);
        }
    }
    
    println("${timeMs/1000.0} seconds")
    var total_inputs_capacity = 0;
    var total_inputs_length = 0;
    var total_users = 0
    for ((roomId, room) in rooms) {
        for ((userId, user) in room.users) {
            total_inputs_capacity += user.inputs.size;
            total_inputs_length += user.inputsSize;
        }
        total_users += room.users.size;
    }
    println("Total rooms: ${rooms.size}")
    println("Total users: $total_users")
    println("Total inputs capacity: $total_inputs_capacity")
    println("Total inputs length:   $total_inputs_length") 
}

// Optimized room joining with minimal allocations
private fun joinRoom(roomId: Int, userId: Int) {
    // Get or create room - O(1) operation
    val room = rooms.getOrPut(roomId) {
        Room(roomId, HashMap(32))
    }
    
    // Add user if not exists - O(1) operation
    room.users.putIfAbsent(userId, User(userId))
}

// Optimized input addition with direct array manipulation
private fun addUserInputs(roomId: Int, userId: Int, inputs: ByteArray) {
    // O(1) room lookup
    val room = rooms[roomId] ?: return
    
    // O(1) user lookup and fast input addition
    room.users[userId]?.addInputs(inputs)
}
