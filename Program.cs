using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;

sealed class User
{
	public readonly int UserId;
	public byte[] Inputs;
	public int InputsLength;

	public User(int userId)
	{
		UserId = userId;
		Inputs = new byte[64];
		InputsLength = 0;
	}
}

sealed class Room
{
	public readonly int RoomId;
	public readonly Dictionary<int, User> Users;

	public Room(int roomId, int initialUsersCapacity)
	{
		RoomId = roomId;
		Users = new Dictionary<int, User>(initialUsersCapacity);
	}
}

static class Program
{
	private static readonly Dictionary<int, Room> Rooms = new Dictionary<int, Room>(1200);

	public static void Main()
	{
		Benchmark();
	}

	[MethodImpl(MethodImplOptions.AggressiveOptimization)]
	private static void Benchmark()
	{
		// Pre-allocate sample inputs to avoid repeated array creation
		byte[] sampleInputs = new byte[10] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

		var sw = Stopwatch.StartNew();

		for (int roomId = 0; roomId < 977; roomId++)
		{
			for (int userId = 0; userId < 173; userId++)
			{
				JoinRoom(roomId, userId);
			}
		}

		for (int i = 0; i < 123_456_789; i++)
		{
			AddUserInputs(i % 997, i % 173, sampleInputs);
		}

		sw.Stop();
		Console.WriteLine(sw.Elapsed);

		long totalInputsCapacity = 0;
		long totalInputsLength = 0;
		int totalUsers = 0;
		foreach (var room in Rooms.Values)
		{
			foreach (var user in room.Users.Values)
			{
				totalInputsCapacity += user.Inputs.Length;
				totalInputsLength += user.InputsLength;
			}
			totalUsers += room.Users.Count;
		}

		Console.WriteLine("Total rooms:  " + Rooms.Count);
		Console.WriteLine("Total users:  " + totalUsers);
		Console.WriteLine("Total inputs capacity:  " + totalInputsCapacity);
		Console.WriteLine("Total inputs length:    " + totalInputsLength);
	}

	[MethodImpl(MethodImplOptions.AggressiveInlining | MethodImplOptions.AggressiveOptimization)]
	private static void JoinRoom(int roomId, int userId)
	{
		ref Room? roomRef = ref CollectionsMarshal.GetValueRefOrAddDefault(Rooms, roomId, out bool roomExists);
		if (!roomExists || roomRef is null)
		{
			roomRef = new Room(roomId, 64);
		}

		var room = roomRef;
		ref User? userRef = ref CollectionsMarshal.GetValueRefOrAddDefault(room.Users, userId, out bool userExists);
		if (!userExists || userRef is null)
		{
			userRef = new User(userId);
		}
	}

	[MethodImpl(MethodImplOptions.AggressiveInlining | MethodImplOptions.AggressiveOptimization)]
	private static void AddUserInputs(int roomId, int userId, byte[] inputs)
	{
		if (!Rooms.TryGetValue(roomId, out var room))
		{
			return; // Room doesn't exist, early exit
		}

		if (!room.Users.TryGetValue(userId, out var user))
		{
			return; // User doesn't exist, early exit
		}

		int requiredLength = user.InputsLength + inputs.Length;
		if (requiredLength > user.Inputs.Length)
		{
			int newCapacity = requiredLength << 1; // (len + inputs) * 2
			var newInputs = new byte[newCapacity];
			Buffer.BlockCopy(user.Inputs, 0, newInputs, 0, user.InputsLength);
			user.Inputs = newInputs;
		}

		Buffer.BlockCopy(inputs, 0, user.Inputs, user.InputsLength, inputs.Length);
		user.InputsLength = requiredLength;
	}
}


