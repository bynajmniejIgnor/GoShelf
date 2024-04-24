package sqlhandler

import (
	"database/sql"
	"encoding/json"
	"log"

	"github.com/labstack/echo/v4"
	_ "github.com/mattn/go-sqlite3"
)

func connect(db_path string) *sql.DB {
	db, err := sql.Open("sqlite3", db_path)
	if err != nil {
		log.Fatal(err)
	}
	return db
}

type request struct {
	User_id string `json:"user_id"`
}

type Shelf struct {
	Name         string
	Books_stored int
}

func GetShelfs(c echo.Context) (string, error) {
	req := new(request)
	if err := c.Bind(req); err != nil {
		return "", err
	}

	db := connect("./sqlhandler/goshelf.db")
	defer db.Close()

	rows, err := db.Query("SELECT name, books_stored FROM shelf WHERE user_id = ?", req.User_id)
	if err != nil {
		log.Fatal(err)
	}
	defer rows.Close()

	var shelfs []Shelf
	var shelf Shelf
	for rows.Next() {
		err := rows.Scan(&shelf.Name, &shelf.Books_stored)
		if err != nil {
			log.Fatal(err)
		}
		shelfs = append(shelfs, shelf)
	}

	jsonData, err := json.Marshal(shelfs)
	if err != nil {
		log.Fatal(err)
	}

	return string(jsonData), nil
}
