# MamGo - AI Coding Instructions

## Project Overview
MamGo is a Go-based HTTP server project that integrates with native C/C++ libraries via CGo for music processing (converting music to "MAMBO" format). The project uses DragonianLib's voice conversion service library for audio processing.

## Architecture

### Component Structure
- **`cmd/api/`**: Application entry point with `main.go` (HTTP server lifecycle) and `test.go` (CGo integration prototype)
- **`internal/server/`**: HTTP server implementation with route handlers and CORS middleware
- **`model/libsvc.win-x64/`**: Native library artifacts (headers, binaries, libs) - shared with `cmd/api/libsvc.win-x64/`

### CGo Integration Pattern
Native library integration uses CGo directives in `cmd/api/test.go`:
```go
// #cgo CPPFLAGS: -g -Wall -I${SRCDIR}/libsvc.win-x64
// #cgo LDFLAGS: -L${SRCDIR}/libsvc.win-x64
// #include "header/NativeApi.h"
import "C"
```
- Libraries are Windows-specific (`.win-x64` suffix throughout)
- DragonianLib API functions use `_Dragonian_Lib_Svc_Add_Prefix()` macro naming convention
- Example: `C._Dragonian_Lib_Svc_Add_Prefix(Init)()` initializes the native library

## Development Workflows

### Build & Run
Use Makefile commands (PowerShell-optimized):
- `make build` - Compiles to `main.exe`
- `make run` - Run directly without building
- `make watch` - Live reload via air (auto-installs if missing)
- `make test` - Run all tests with verbose output

### Environment Configuration
Server config via `.env` file (loaded with `godotenv/autoload`):
```
ADDRESS=localhost
PORT=8080
APP_ENV=local
```

### Server Lifecycle
- Graceful shutdown with 5-second timeout (`cmd/api/main.go`)
- Signal handling for `SIGINT`/`SIGTERM` with context-based coordination
- HTTP server config: 1min idle, 10s read, 30s write timeouts

## Code Conventions

### HTTP Handlers
- Methods on `Server` struct: `func (s *Server) HandlerName(w http.ResponseWriter, r *http.Request)`
- JSON responses via `json.Marshal()` with explicit Content-Type headers
- Error handling: log then return HTTP error response
- Example pattern in `internal/server/routes.go`:
```go
jsonResp, err := json.Marshal(resp)
if err != nil {
    http.Error(w, "Failed to marshal response", http.StatusInternalServerError)
    return
}
w.Header().Set("Content-Type", "application/json")
```

### CORS Middleware
Global CORS policy wraps all routes via `corsMiddleware()`:
- Wildcard origin (`*`) - update for production
- Supports all common HTTP methods
- Handles preflight OPTIONS requests

### Testing
- Use `httptest.NewServer()` for handler testing
- Test file naming: `*_test.go` (excluded from air watch)
- Note: Current test in `routes_test.go` expects `"Hello World"` but actual handler returns `"Why are you here?"` - **test needs update**

## Critical Dependencies
- `github.com/joho/godotenv` - Auto-loaded in `internal/server/server.go`
- Air (optional) - Live reload for development
- DragonianLib - Native voice conversion library (AGPL-3.0 licensed)

## Platform Constraints
- **Windows-only**: Native libraries are win-x64 specific
- PowerShell shell assumed for Makefile targets
- Uses `.exe` extension for built binaries

## Known Issues & TODOs
1. `test.go` has `notmain()` function (should be `main()` or removed?)
2. Native library initialization commented out in `test.go`
3. Test expectations don't match actual handler responses
4. Duplicate `libsvc.win-x64` directories in `cmd/api/` and `model/` (consolidation needed?)
