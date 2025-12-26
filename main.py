import time

class User:
    __slots__ = ['user_id', 'inputs']
    
    def __init__(self, user_id):
        self.user_id = user_id
        self.inputs = bytearray()  # More efficient for uint8 operations than list

class Room:
    __slots__ = ['room_id', 'users']
    
    def __init__(self, room_id):
        self.room_id = room_id
        self.users = {}  # O(1) user lookup instead of O(n) list search

# Global room storage for O(1) room access
rooms = {}

def main():
    benchmark()

def benchmark():
    # Pre-allocate sample inputs to avoid repeated creation
    sample_inputs = bytes([1, 2, 3, 4, 5, 6, 7, 8, 9, 10])
    
    start_time = time.perf_counter()
    
    for room_id in range(977):
        for user_id in range(173):
            join_room(room_id, user_id);
    
    for i in range(123_456_789):
        add_user_inputs(i % 977, i % 173, sample_inputs);
    
    end_time = time.perf_counter()
    
    print(f"{end_time - start_time:.9f}")

    total_inputs_length = 0;
    total_users = 0
    for roomId in rooms:
        room = rooms[roomId];
        for userId in room.users:
            user = room.users[userId];
            total_inputs_length += len(user.inputs);
        total_users += len(room.users);
    print("Total rooms: ", len(rooms));
    print("Total users: ", total_users);
    print("Total inputs length:   ", total_inputs_length);


def join_room(room_id, user_id):
    # Get existing room or create new one - O(1) operation
    if room_id not in rooms:
        # Create new room
        rooms[room_id] = Room(room_id)
    
    room = rooms[room_id]
    
    # Check if user already exists to avoid duplicates
    if user_id not in room.users:
        # Add new user
        room.users[user_id] = User(user_id)

def add_user_inputs(room_id, user_id, inputs):
    # O(1) room lookup using dict
    room = rooms.get(room_id)
    if room is None:
        return  # Room doesn't exist, early exit
    
    # O(1) user lookup using dict instead of O(n) list search
    user = room.users.get(user_id)
    if user is not None:
        user.inputs.extend(inputs)

if __name__ == "__main__":
    main()
