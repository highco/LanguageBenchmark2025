use std::time::Instant;

use dashmap::DashMap;
use once_cell::sync::Lazy;

struct User {
	user_id: i32,
	inputs: Vec<u8>,
}

struct Room {
	room_id: i32,
	users: Vec<User>,
}

// Global room storage using DashMap for lock-free concurrent access
static ROOMS: Lazy<DashMap<i32, Room>> = Lazy::new(|| {
	DashMap::with_capacity(1024) // Pre-allocate for better performance
});

fn main() {
	benchmark();
}

fn benchmark() {
	let sample_inputs = vec![1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
	let start = Instant::now();
	for i in 0..50_000_000 {
		join_room(i % 17, i % 997);
	}

	for i in 0..50_000_000 {
		add_user_input(i % 17, i % 997, &sample_inputs);
	}
	let end = Instant::now();
	println!("Time taken: {:?}", end.duration_since(start));
}

fn join_room(user_id: i32, room_id: i32) {
	// Fast path: check if room exists and add user directly
	if let Some(mut room_ref) = ROOMS.get_mut(&room_id) {
		// Room exists, add user directly to avoid reallocation
		room_ref.users.push(User {
			user_id,
			inputs: Vec::with_capacity(64), // Pre-allocate some capacity
		});
	} else {
		// Room doesn't exist, create new room with the user
		let new_room = Room {
			room_id,
			users: {
				let mut users = Vec::with_capacity(16); // Pre-allocate for multiple users
				users.push(User {
					user_id,
					inputs: Vec::with_capacity(64),
				});
				users
			},
		};
		ROOMS.insert(room_id, new_room);
	}
}

fn add_user_input(user_id: i32, room_id: i32, inputs: &[u8]) {
	if let Some(mut room_ref) = ROOMS.get_mut(&room_id) {
		if let Some(user) = room_ref.users.iter_mut().find(|u| u.user_id == user_id) {
			user.inputs.extend(inputs);
		}
	}
}