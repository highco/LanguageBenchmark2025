import time
from collections import defaultdict

class User:
    __slots__ = ['user_id', 'inputs']
    
    def __init__(self, user_id):
        self.user_id = user_id
        self.inputs = bytearray()

class Room:
    __slots__ = ['room_id', 'users']
    
    def __init__(self, room_id):
        self.room_id = room_id
        self.users = {}

# Global room storage - using defaultdict for even faster access
rooms = {}

def main():
    benchmark()

def benchmark():
    # Pre-allocate sample inputs as bytes for maximum efficiency
    sample_inputs = bytes([1, 2, 3, 4, 5, 6, 7, 8, 9, 10])
    
    start_time = time.perf_counter()
    
    # Optimized loops with local variable references
    join_room_func = join_room
    add_user_inputs_func = add_user_inputs
    
    # First loop: join rooms - using local variables for faster access
    for i in range(50_000_000):
        join_room_func(i % 17, i % 977)
    
    # Second loop: add user inputs
    for i in range(50_000_000):
        add_user_inputs_func(i % 17, i % 977, sample_inputs)
    
    end_time = time.perf_counter()
    
    print(f"{end_time - start_time:.9f}")

def join_room(room_id, user_id):
    # Optimized room creation and user addition
    try:
        room = rooms[room_id]
    except KeyError:
        room = Room(room_id)
        rooms[room_id] = room
    
    # Use try/except instead of 'in' check for potentially better performance
    try:
        _ = room.users[user_id]
    except KeyError:
        room.users[user_id] = User(user_id)

def add_user_inputs(room_id, user_id, inputs):
    # Optimized with try/except for better performance
    try:
        room = rooms[room_id]
        user = room.users[user_id]
        user.inputs.extend(inputs)
    except KeyError:
        pass  # Room or user doesn't exist, skip

if __name__ == "__main__":
    main()