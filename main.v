module main

import time

// Use struct for User to match JS class
// Optimized by using V's native dynamic array which handles resizing
struct User {
mut:
	user_id int
	inputs  []u8
}

// Use struct for Room to match JS class
struct Room {
mut:
	room_id int
	users   map[int]&User
}

// Global state container
struct Context {
mut:
	rooms map[int]&Room
}

fn main() {
	mut ctx := Context{
		rooms: map[int]&Room{}
	}
	benchmark(mut ctx)
}

fn benchmark(mut ctx Context) {
	// Pre-allocate sample inputs
	sample_inputs := [u8(1), 2, 3, 4, 5, 6, 7, 8, 9, 10]

	sw := time.new_stopwatch()

	// Create rooms and users
	for room_id := 0; room_id < 977; room_id++ {
		for user_id := 0; user_id < 173; user_id++ {
			join_room(mut ctx, room_id, user_id)
		}
	}

	// Add inputs
	for i := 0; i < 123_456_789; i++ {
		add_user_inputs(mut ctx, i % 997, i % 173, sample_inputs)
	}

	elapsed := sw.elapsed().seconds()
	println('${elapsed} seconds')

	// Calculate stats
	mut total_inputs_capacity := i64(0)
	mut total_inputs_length := i64(0)
	mut total_users := 0

	for _, room in ctx.rooms {
		for _, user in room.users {
			total_inputs_capacity += user.inputs.cap
			total_inputs_length += user.inputs.len
		}
		total_users += room.users.len
	}

	println('Total rooms: ${ctx.rooms.len}')
	println('Total users: ${total_users}')
	println('Total inputs capacity: ${total_inputs_capacity}')
	println('Total inputs length:   ${total_inputs_length}')
}

// Optimized joinRoom
// Uses direct map access and pointers to avoid copying
@[inline]
fn join_room(mut ctx Context, room_id int, user_id int) {
	// Check if room exists; if not, create and insert it
	mut room := ctx.rooms[room_id] or {
		new_room := &Room{
			room_id: room_id
			users: map[int]&User{}
		}
		ctx.rooms[room_id] = new_room
		new_room
	}
	
	// Create user with pre-allocated capacity
	room.users[user_id] = &User{
		user_id: user_id
		inputs: []u8{cap: 64} // Larger pre-allocation as in JS
	}
}

// Optimized addUserInputs
// Uses direct map access and V's efficient array appending
@[inline]
fn add_user_inputs(mut ctx Context, room_id int, user_id int, inputs []u8) {
	mut room := ctx.rooms[room_id] or { return }
	mut user := room.users[user_id] or { return }
	
	// V's << operator handles capacity checks and resizing efficiently
	// This replaces the manual Uint8Array resizing logic in JS
	user.inputs << inputs
}
