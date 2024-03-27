package main

import (
	"GoShelf/server"
	"net/http"

	"github.com/labstack/echo/v4"
)

type OKresponse struct {
	Ocr string `json:"ocr"`
}

type ErrorResponse struct {
	Error string `json:"error"`
	Msg   string `json:"msg"`
}

func main() {
	e := echo.New()

	e.POST("/process", func(c echo.Context) error {
		body, err := server.ProcessImage(c)
		if err != nil {
			return c.JSON(http.StatusInternalServerError, ErrorResponse{Error: err.Error(), Msg: body})
		}
		return c.JSON(http.StatusOK, OKresponse{Ocr: body})
	})

	e.Logger.Fatal(e.Start(":8080"))
}
