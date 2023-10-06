package server

import (
	"faas/pkg/instance"
	"github.com/apibrew/apibrew/pkg/client"
	"time"
)

type Server struct {
}

func NewServer() *Server {
	return &Server{}
}

func (s *Server) Start() error {
	inst, err := instance.NewInstanceClient(client.ServerConfig{
		Host: "localhost:9009",
		Authentication: client.ConfigServerAuthentication{
			Username: "admin",
			Password: "admin",
		},
		Insecure: true,
	})

	if err = inst.Init(); err != nil {
		panic(err)
	}

	if err != nil {
		return err
	}

	time.Sleep(1000000000000000000)

	return nil
}
