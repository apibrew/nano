package server

import server2 "faas/pkg/server"

func main() {
	server := server2.NewServer()

	err := server.Start()
	if err != nil {
		panic(err)
	}
}
