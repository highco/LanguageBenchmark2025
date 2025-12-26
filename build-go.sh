#!/bin/bash

# Build Go with maximum optimization for speed
go build -o bin/go -ldflags="-s -w" -gcflags="-l=4 -B" main.go
