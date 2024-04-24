package sqlhandler

import (
	"database/sql"
	"encoding/json"
	"log"

	_ "github.com/mattn/go-sqlite3"
)

func connect(db_path string) *sql.DB {
	db, err := sql.Open("sqlite3", db_path)
	if err != nil {
		log.Fatal(err)
	}
	return db
}

type Shelf struct {
	Name         string
	Books_stored int
}

func GetShelves(user_id string) (string, error) {
	db := connect("./sqlhandler/goshelf.db")
	defer db.Close()

	rows, err := db.Query("SELECT name, books_stored FROM shelf WHERE user_id = ?", user_id)
	if err != nil {
		log.Fatal(err)
	}
	defer rows.Close()

	var shelves []Shelf
	var shelf Shelf
	for rows.Next() {
		err := rows.Scan(&shelf.Name, &shelf.Books_stored)
		if err != nil {
			log.Fatal(err)
		}
		shelves = append(shelves, shelf)
	}

	jsonData, err := json.Marshal(shelves)
	if err != nil {
		log.Fatal(err)
	}

	return string(jsonData), nil
}
