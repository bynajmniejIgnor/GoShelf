package main

import (
	"fmt"
	"goshelf/sqlhandler"
	"net/http"
	"net/url"

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

	e.GET("/androidId/:android_id", func(c echo.Context) error {
		android_id := c.Param("android_id")
		user_id := sqlhandler.GetUserIdByAndroidId(android_id)
		if user_id != "" {
			return c.JSON(http.StatusOK, OKresponse{JsonData: user_id})
		}
		return c.JSON(http.StatusNotFound, ErrorResponse{Error: "AndroidId not found", Msg: ""})
	})

	e.GET("/setAndroidId/:user_id/:android_id", func(c echo.Context) error {
		user_id := c.Param("user_id")
		android_id := c.Param(("android_id"))

		err := sqlhandler.SetAndroidId(user_id, android_id)
		if err != nil {
			return c.JSON(http.StatusInternalServerError, ErrorResponse{Error: err.Error(), Msg: ""})
		}
		return c.JSON(http.StatusOK, OKresponse{JsonData: "android id set successfully"})
	})

	e.GET("addShelf/:user_id/:name", func(c echo.Context) error {
		user_id := c.Param("user_id")
		name := c.Param("name")

		shelf_id, err := sqlhandler.AddShelf(name, user_id)
		if err != nil {
			return c.JSON(http.StatusInternalServerError, ErrorResponse{Error: err.Error(), Msg: ""})
		}
		return c.JSON(http.StatusOK, OKresponse{JsonData: shelf_id})
	})

	e.GET("deleteShelf/:shelf_id", func(c echo.Context) error {
		shelf_id := c.Param("shelf_id")

		err := sqlhandler.DeleteShelf(shelf_id)
		if err != nil {
			return c.JSON(http.StatusInternalServerError, ErrorResponse{Error: err.Error(), Msg: ""})
		}
		return c.JSON(http.StatusOK, OKresponse{JsonData: "shelf deleted"})
	})

	e.GET("/search/:object/:user_id/:query", func(c echo.Context) error {
		object := c.Param("object")
		query, _ := url.QueryUnescape(c.Param("query"))
		user_id := c.Param("user_id")

		if object == "shelf" {
			resp, err := sqlhandler.ShelfSearch(user_id, query)
			if err != nil {
				return c.JSON(http.StatusInternalServerError, ErrorResponse{Error: err.Error(), Msg: resp})
			}
			return c.JSON(http.StatusOK, OKresponse{JsonData: resp})
		} else if object == "book" {
			resp, err := sqlhandler.BookSearch(query)
			if err != nil {
				return c.JSON(http.StatusInternalServerError, ErrorResponse{Error: err.Error(), Msg: resp})
			}
			return c.JSON(http.StatusOK, OKresponse{JsonData: resp})
		}
		return fmt.Errorf("big nono")
	})

	e.GET("/addBook/:shelf_id/:title/:subtitle/:authors", func(c echo.Context) error {
		shelf_id := c.Param("shelf_id")
		title, _ := url.QueryUnescape(c.Param("title"))
		subtitle, _ := url.QueryUnescape(c.Param("subtitle"))
		authors, _ := url.QueryUnescape(c.Param("authors"))

		if subtitle == "null" {
			subtitle = ""
		}

		rowid, err := sqlhandler.AddBook(shelf_id, title, subtitle, authors)
		if err != nil {
			return c.JSON(http.StatusInternalServerError, ErrorResponse{Error: err.Error(), Msg: ""})
		}
		return c.JSON(http.StatusOK, OKresponse{JsonData: rowid})
	})

	e.GET("/shelfName/:shelf_id", func(c echo.Context) error {
		shelf_id := c.Param("shelf_id")
		shelf_name, err := sqlhandler.GetShelfName(shelf_id)
		if err != nil {
			return c.JSON(http.StatusInternalServerError, ErrorResponse{Error: err.Error(), Msg: ""})
		}
		return c.JSON(http.StatusOK, OKresponse{JsonData: shelf_name})
	})

	e.GET("/deleteBook/:book_id", func(c echo.Context) error {
		book_id := c.Param("book_id")
		err := sqlhandler.DeleteBook(book_id)
		if err != nil {
			return c.JSON(http.StatusInternalServerError, ErrorResponse{Error: err.Error(), Msg: ""})
		}
		return c.JSON(http.StatusOK, OKresponse{JsonData: "The book is no more"})
	})

	e.Logger.Fatal(e.Start(":8080"))
}
