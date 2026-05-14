package main

// Import any modules required
import "fmt"

// #cgo CPPFLAGS: -g -Wall -I${SRCDIR}/libsvc.win-x64
// #cgo LDFLAGS: -L${SRCDIR}/libsvc.win-x64
// #include "header/NativeApi.h"
// #include <stdio.h>
import "C"

// Define the main function
func notmain() {
	fmt.Println("Hello World")
	// C._Dragonian_Lib_Svc_Add_Prefix(C.Init)()
}
