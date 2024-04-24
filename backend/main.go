package main

import (
	"goshelf/sqlhandler"
	"net/http"

	"github.com/labstack/echo/v4"
)

type OKresponse struct {
	JsonData string `json:"response"`
}

type ErrorResponse struct {
	Error string `json:"error"`
	Msg   string `json:"msg"`
}

func main() {
	e := echo.New()

	e.POST("/shelfs", func(c echo.Context) error {
		resp, err := sqlhandler.GetShelfs(c)
		if err != nil {
			return c.JSON(http.StatusInternalServerError, ErrorResponse{Error: err.Error(), Msg: resp})
		}
		return c.JSON(http.StatusOK, OKresponse{JsonData: resp})
	})

	e.Logger.Fatal(e.Start(":8080"))
}
