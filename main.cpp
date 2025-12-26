#include <iostream>
#include <unordered_map>
#include <vector>
#include <chrono>
#include <cstdint>

struct User {
    int user_id;
    std::vector<uint8_t> inputs;
    
    User(int id) : user_id(id) {
        inputs.reserve(64); // Pre-allocate for 64 operations * 10 inputs
    }
};

struct Room {
    int room_id;
    std::unordered_map<int, User*> users;
    
    Room(int id) : room_id(id) {
        users.reserve(64); // Pre-allocate for expected number of users
    }
    
    ~Room() {
        // Clean up users
        for (auto& pair : users) {
            delete pair.second;
        }
    }
};

// Global room storage for O(1) room access
std::unordered_map<int, Room*> rooms;

void join_room(int room_id, int user_id) {
    // Get existing room or create new one - O(1) operation
    Room* room;
    auto room_it = rooms.find(room_id);
    if (room_it == rooms.end()) {
        // Create new room with pre-allocated user map
        room = new Room(room_id);
        rooms[room_id] = room;
    } else {
        room = room_it->second;
    }
    
    room->users[user_id] = new User(user_id);
}

void add_user_inputs(int room_id, int user_id, const std::vector<uint8_t>& inputs) {
    // O(1) room lookup using unordered_map
    auto room_it = rooms.find(room_id);
    if (room_it == rooms.end()) {
        return; // Room doesn't exist, early exit
    }
    
    Room* room = room_it->second;
    
    // O(1) user lookup using unordered_map instead of O(n) linear search
    auto user_it = room->users.find(user_id);
    if (user_it != room->users.end()) {
        User* user = user_it->second;
        
        // Check capacity and pre-allocate if needed to minimize reallocations
        size_t current_size = user->inputs.size();
        size_t needed_capacity = current_size + inputs.size();
        
        if (user->inputs.capacity() < needed_capacity) {
            // Need more capacity - allocate with room to grow
            user->inputs.reserve(needed_capacity*2);
        }
        
        user->inputs.insert(user->inputs.end(), inputs.begin(), inputs.end());
    }
}

void benchmark() {
    // Pre-allocate sample inputs to avoid repeated vector creation
    const std::vector<uint8_t> sample_inputs = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    
    auto start_time = std::chrono::high_resolution_clock::now();
    
    for(int room_id = 0; room_id < 977; ++room_id) {
        for(int user_id = 0; user_id < 173; ++user_id) {
            join_room(room_id, user_id);
        }
    }
    
    for (int i = 0; i < 123456789; ++i) {
        add_user_inputs(i % 977, i % 173, sample_inputs);
    }
    
    auto end_time = std::chrono::high_resolution_clock::now();
    
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end_time - start_time);
    std::cout << duration.count()/1000.0 << " seconds" << std::endl;

    int total_inputs_capacity = 0;
    int total_inputs_length = 0;
    int total_users = 0;
    for (auto& pair : rooms) {
        Room* room = pair.second;
        for (auto& pair : room->users) {
            User* user = pair.second;
            total_inputs_capacity += user->inputs.capacity();
            total_inputs_length += user->inputs.size();
        }
        total_users += room->users.size();
    }
    std::cout << "Total rooms: " << rooms.size() << std::endl;
    std::cout << "Total users: " << total_users << std::endl;
    std::cout << "Total inputs capacity: " << total_inputs_capacity << std::endl;
    std::cout << "Total inputs length:   " << total_inputs_length << std::endl;
}

void cleanup() {
    // Clean up all rooms
    for (auto& pair : rooms) {
        delete pair.second;
    }
    rooms.clear();
}

int main() {
    benchmark();
    cleanup();
    return 0;
}
