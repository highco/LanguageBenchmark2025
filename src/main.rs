


use std::time::Instant;

use dashmap::DashMap;
use once_cell::sync::Lazy;
use rustc_hash::FxHashMap;

#[allow(dead_code)]
struct User {
	user_id: i32,
	inputs: Vec<u8>,
}

#[allow(dead_code)]
struct Room {
	room_id: i32,
	users: FxHashMap<i32, User>, // O(1) user lookups instead of O(n)
}

// Global room storage using DashMap for lock-free concurrent access
static ROOMS: Lazy<DashMap<i32, Room>> = Lazy::new(|| {
	DashMap::with_capacity(64) // Larger pre-allocation
});

fn main() {
	benchmark();
}

fn benchmark() {
	let sample_inputs = vec![1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
	let start = Instant::now();

	for room_id in 0..977 {
        for user_id in 0..173 {
            join_room(room_id, user_id);
        }
    }
    
    for i in 0..123_456_789 {
        add_user_input(i % 977, i % 173, &sample_inputs);
    }

	let end = Instant::now();
	println!("Time taken: {:?}", end.duration_since(start));

    let mut total_inputs_capacity = 0;
    let mut total_inputs_length = 0;
    let mut total_users = 0;
    for room_ref in ROOMS.iter() {
        for (_, user) in &room_ref.users {
            total_inputs_capacity += user.inputs.capacity();
            total_inputs_length += user.inputs.len();
        }
        total_users += room_ref.users.len();
    }
    println!("Total rooms: {}", ROOMS.len());
    println!("Total users: {}", total_users);
    println!("Total inputs capacity: {}", total_inputs_capacity);
    println!("Total inputs length:   {}", total_inputs_length);

}

fn join_room(room_id: i32,user_id: i32) {
	// Fast path: check if room exists and add user directly
	if let Some(mut room_ref) = ROOMS.get_mut(&room_id) {
		// Room exists, add user directly with O(1) insertion
		room_ref.users.insert(user_id, User {
			user_id,
			inputs: Vec::with_capacity(64), // Larger pre-allocation
		});
	} else {
		// Room doesn't exist, create new room with the user
		let mut users = FxHashMap::with_capacity_and_hasher(32, Default::default());
		users.insert(user_id, User {
			user_id,
			inputs: Vec::with_capacity(64), // Larger pre-allocation
		});
		
		let new_room = Room {
			room_id,
			users,
		};
		ROOMS.insert(room_id, new_room);
	}
}

fn add_user_input(room_id: i32, user_id: i32, inputs: &[u8]) {
	if let Some(mut room_ref) = ROOMS.get_mut(&room_id) {
		// O(1) user lookup instead of O(n) linear search
		if let Some(user) = room_ref.users.get_mut(&user_id) {
			// Reserve capacity to minimize reallocations
			let current_len = user.inputs.len();
			let needed_capacity = current_len + inputs.len();
			if user.inputs.capacity() < needed_capacity {
				user.inputs.reserve_exact(needed_capacity*2 - user.inputs.len()); // Reserve more than needed
			}
			user.inputs.extend_from_slice(inputs); // Slightly faster than extend()
		}
	}
}
