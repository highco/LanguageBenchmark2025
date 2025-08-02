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

	for i := 0; i < 50_000_000; i++ {
		join_room(i%17, i%997)
	}

	for i := 0; i < 50_000_000; i++ {
		add_user_inputs(i%17, i%997, sample_inputs[:])
	}

	var endTime = time.Now()

	fmt.Println(endTime.Sub(startTime))
}

func join_room(room_id int, user_id int) {
	// Get existing room or create new one - O(1) operation
	room, exists := rooms[room_id]
	if !exists {
		// Create new room with pre-allocated user map
		room = &Room{
			room_id: room_id,
			users:   make(map[int]*User, 32), // Pre-allocate for expected number of users
		}
		rooms[room_id] = room
	}

	// Check if user already exists to avoid duplicates
	if _, exists := room.users[user_id]; !exists {
		// Add new user with pre-allocated input capacity
		room.users[user_id] = &User{
			user_id: user_id,
			Inputs:  make([]uint8, 0, 128), // Pre-allocate for 64 operations * 10 inputs
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
			newInputs := make([]uint8, len(user.Inputs), (len(user.Inputs)+len(inputs))+100)
			copy(newInputs, user.Inputs)
			user.Inputs = newInputs
		}
		user.Inputs = append(user.Inputs, inputs...)
	}
}
