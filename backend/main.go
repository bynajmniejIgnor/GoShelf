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
	Msg   string `json:"response"`
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

	e.GET("/books/:shelf_id", func(c echo.Context) error {
		shelf_id := c.Param("shelf_id")
		resp, err := sqlhandler.GetBooks(shelf_id)
		if err != nil {
			return c.JSON(http.StatusInternalServerError, ErrorResponse{Error: err.Error(), Msg: resp})
		}
		return c.JSON(http.StatusOK, OKresponse{JsonData: resp})
	})

	e.GET("/login/:username/:hash", func(c echo.Context) error {
		username := c.Param("username")
		hash := c.Param("hash")

		resp, err := sqlhandler.GetUserID(username, hash)
		if err != nil {
			return c.JSON(http.StatusInternalServerError, ErrorResponse{Error: err.Error(), Msg: string(resp)})
		}
		return c.JSON(http.StatusOK, OKresponse{JsonData: string(resp)})

	})

	e.Logger.Fatal(e.Start(":8080"))
}
