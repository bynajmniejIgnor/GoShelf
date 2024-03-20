package main

import (
	"GoShelf/server"
	"net/http"

	"github.com/labstack/echo/v4"
)

func main() {
	/*
		encoded := api.LoadFile("test.png")
		file, err := server.SaveFile(encoded)

		if err != nil {
			panic(err)
		}
		fmt.Println("File saved as", file)
	*/
	e := echo.New()

	e.POST("/process", func(c echo.Context) error {
		body, err := server.ProcessImage(c)
		if err != nil {
			return c.String(http.StatusBadRequest, "{\"msg\": "+err.Error()+", \"body\": "+body+"}")
		}
		return c.JSON(http.StatusOK, body)
	})

	e.Logger.Fatal(e.Start(":8080"))
}
