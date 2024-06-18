package main

import "C"

func main() {
	println("Hello, world!")
}

//export DoPrintHelloWorld
func DoPrintHelloWorld() *C.char {
	return C.CString("Hello, world! From dynamic library.")
}
