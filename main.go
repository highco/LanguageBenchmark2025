package main

import (
	"fmt"
	"time"
)

type User struct {
	user_id int
	Inputs  []uint8
}

type Room struct {
	room_id int
	users   map[int]*User // O(1) user lookup instead of O(n) slice search
}

// Global room storage for O(1) room access
var rooms = make(map[int]*Room)

func main() {
	benchmark()
}

func benchmark() {
	// Pre-allocate sample inputs to avoid repeated slice creation
	sample_inputs := [10]uint8{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}

	var startTime = time.Now()

	for room_id := 0; room_id < 977; room_id++ {
        for user_id := 0; user_id < 173; user_id++ {
            join_room(room_id, user_id);
        }
    }
    
    for i := 0; i < 123_456_789; i++ {
        add_user_inputs(i % 997, i % 173, sample_inputs[:]);
    }

	var endTime = time.Now()

	fmt.Println(endTime.Sub(startTime))

	total_inputs_capacity := 0;
    total_inputs_length := 0;
    total_users := 0
    for roomId := range rooms {
        room := rooms[roomId];
        for userId := range room.users {
            user := room.users[userId];
            total_inputs_capacity += cap(user.Inputs);
            total_inputs_length += len(user.Inputs);
        }
        total_users += len(room.users);
    }
    fmt.Println("Total rooms: ", len(rooms));
    fmt.Println("Total users: ", total_users);
    fmt.Println("Total inputs capacity: ", total_inputs_capacity);
    fmt.Println("Total inputs length:   ", total_inputs_length);

}

func join_room(room_id int, user_id int) {
	// Get existing room or create new one - O(1) operation
	room, exists := rooms[room_id]
	if !exists {
		// Create new room with pre-allocated user map
		room = &Room{
			room_id: room_id,
			users:   make(map[int]*User, 64), // Pre-allocate for expected number of users
		}
		rooms[room_id] = room
	}

	// Check if user already exists to avoid duplicates
	if _, exists := room.users[user_id]; !exists {
		// Add new user with pre-allocated input capacity
		room.users[user_id] = &User{
			user_id: user_id,
			Inputs:  make([]uint8, 0, 64), // Pre-allocate for 64 operations * 10 inputs
		}
	}
}

func add_user_inputs(room_id int, user_id int, inputs []uint8) {
	// O(1) room lookup using map
	room := rooms[room_id]
	if room == nil {
		return // Room doesn't exist, early exit
	}

	// O(1) user lookup using map instead of O(n) slice search
	user := room.users[user_id]
	if user != nil {
		// Check capacity and pre-allocate if needed to minimize reallocations
		if cap(user.Inputs)-len(user.Inputs) < len(inputs) {
			// Need more capacity - allocate with room to grow
			newInputs := make([]uint8, len(user.Inputs), (len(user.Inputs)+len(inputs))*2)
			copy(newInputs, user.Inputs)
			user.Inputs = newInputs
		}
		user.Inputs = append(user.Inputs, inputs...)
	}
}
