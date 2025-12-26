// Use plain objects for even faster access in V8 when keys are integers
class User {
    constructor(userId) {
        this.user_id = userId;
        // Pre-allocate Uint8Array with larger initial capacity
        this.inputs = new Uint8Array(64); // Larger pre-allocation
        this.inputsLength = 0;
    }
}

class Room {
    constructor(roomId) {
        this.room_id = roomId;
        // Use plain object for integer keys - V8 optimizes this very well
        this.users = Object.create(null); // Faster than {} for pure data storage
    }
}

// Global room storage using plain object for integer keys
const rooms = Object.create(null);

function main() {
    benchmark();
}

function benchmark() {
    // Pre-allocate sample inputs as Uint8Array to avoid repeated allocation
    const sampleInputs = new Uint8Array([1, 2, 3, 4, 5, 6, 7, 8, 9, 10]);
    
    const startTime = process.hrtime.bigint();
    
    for(let room_id = 0; room_id < 977; ++room_id) {
        for(let user_id = 0; user_id < 173; ++user_id) {
            joinRoom(room_id, user_id);
        }
    }
    
    for (let i = 0; i < 123_456_789; i++) {
        addUserInputs(i % 977, i % 173, sampleInputs);
    }
    
    const endTime = process.hrtime.bigint();
    
    // Convert nanoseconds to milliseconds for display
    const duration = Number(endTime - startTime) / 1_000_000;
    console.log(`${duration/1000} seconds`);

    let total_inputs_capacity = 0;
    let total_inputs_length = 0;
    let total_users = 0
    for (let roomId in rooms) {
        let room = rooms[roomId];
        for (let userId in room.users) {
            let user = room.users[userId];
            total_inputs_capacity += user.inputs.length;
            total_inputs_length += user.inputsLength;
        }
        total_users += Object.keys(room.users).length;
    }
    console.log(`Total rooms: ${Object.keys(rooms).length}`);
    console.log(`Total users: ${total_users}`);
    console.log(`Total inputs capacity: ${total_inputs_capacity}`);
    console.log(`Total inputs length:   ${total_inputs_length}`);
}

function joinRoom(roomId, userId) {
    // Get existing room or create new one - O(1) operation with object property access
    let room = rooms[roomId];
    if (!room) {
        // Create new room
        room = new Room(roomId);
        rooms[roomId] = room;
    }
    
    room.users[userId] = new User(userId);
}

function addUserInputs(roomId, userId, inputs) {
    // O(1) room lookup using object property access
    const room = rooms[roomId];
    if (!room) {
        return; // Room doesn't exist, early exit
    }
    
    // O(1) user lookup using object property access
    const user = room.users[userId];
    if (user) {
        // Check if we need to resize the Uint8Array
        if (user.inputsLength + inputs.length > user.inputs.length) {
            // Need more capacity - allocate new larger array
            const newSize = (user.inputsLength + inputs.length)*2;
            const newInputs = new Uint8Array(newSize);
            newInputs.set(user.inputs.subarray(0, user.inputsLength));
            user.inputs = newInputs;
        }
        
        // Copy inputs efficiently using set method
        user.inputs.set(inputs, user.inputsLength);
        user.inputsLength += inputs.length;
    }
}

// Run the benchmark
main();
