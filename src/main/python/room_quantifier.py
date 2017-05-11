#!/usr/bin/python
# -*- coding: utf-8 -*-

import glob
import sys

WALL = 1

def calculateOpenness(room):
	'''
	Calculates the openness of a room
	'''
	metrics = [0, 0, 0, 0, 0, 0, 0, 0]
	try:
		for i in range(0, len(room)):
			for j in range(0, len(room[0])):
				n = calculateNeighbours(room, i, j)
				if n > 0:
					metrics[n - 1] += 1
	except:
		print("Malformed input file")
		sys.exit()

	return metrics

def calculateNeighbours(room, x, y):
	'''
	Calculates the number of open neighbous a space has
	'''
	nbr_of_neighbours = 0
	start_x = 0
	start_y = 0
	end_x = len(room) - 1
	end_y = len(room[0]) - 1
	
	if room[x][y] != WALL:
		if x > start_x:
			start_x = x - 1
		if x < end_x:
			end_x = x + 1
		if y > start_y:
			start_y = y - 1
		if y < end_y:
			end_y = y + 1
		for i in range(start_x, end_x + 1):
			for j in range(start_y, end_y + 1):
				if room[i][j] != WALL and not (x == i and y == j):
					nbr_of_neighbours += 1
	
	return nbr_of_neighbours

def getMapFromFile(file_name) :
	'''
	Tries to read a room from a file
	'''
	room = []
	try:
		with open(file_name) as f:
			content = f.readlines()
		lines = [x.strip() for x in content]
		room = [0] * len(lines)
		for i in range(0, len(lines)):
			room[i] = [0] * len(lines[0])
			for j in range(0, len(lines[0])):
				room[i][j] = int(lines[i][j], 16)
		return room
	except IOError:
		print("No such file. You'd better provide a valid file name")
		sys.exit()
	except:
		print("Malformed input file")
		sys.exit()
	return None

def getMapsFromDirectory(directory):
	'''
	Converts all files 
	'''
	files = glob.glob(directory + "/*.txt")
	rooms = dict()
	for file_name in files:
		rooms[file_name] = getMapFromFile(file_name);
	return rooms

def fromMapsToResults(rooms):
	for room in rooms:
		rooms[room] = calculateOpenness(rooms[room])
	return rooms

def printResults(rooms):
	for room in rooms:
		print(room, ": ", rooms[room])

def main():
	if len(sys.argv) < 2:
		print("Please provide a directory name")
	else:
		dir_name = sys.argv[1]
		fileRooms = getMapsFromDirectory(dir_name)
		results = fromMapsToResults(fileRooms)
		printResults(results)

main()
