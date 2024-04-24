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

	e.GET("/shelves/:user_id", func(c echo.Context) error {
		user_id := c.Param("user_id")
		resp, err := sqlhandler.GetShelves(user_id)
		if err != nil {
			return c.JSON(http.StatusInternalServerError, ErrorResponse{Error: err.Error(), Msg: resp})
		}
		return c.JSON(http.StatusOK, OKresponse{JsonData: resp})
	})

	e.Logger.Fatal(e.Start(":8080"))
}
