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
	_, err = db.Query("PRAGMA foreign_keys = ON;")
	if err != nil {
		log.Fatal(err)
	}
	return db
}

type Shelf struct {
	Name         string
	Books_stored int
}

type Book struct {
	Title, Subtitle, Authors string
}

func GetShelves(user_id string) (string, error) {
	db := connect("./sqlhandler/goshelf.db")
	defer db.Close()

	rows, err := db.Query("SELECT name, books_stored FROM shelves WHERE user_id = ?", user_id)
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

func GetBooks(shelf_id int) (string, error) {
	db := connect("./sqlhandler/goshelf.db")
	defer db.Close()

	rows, err := db.Query("SELECT title, subtitle, authors FROM books WHERE shelf_id = ?", shelf_id)
	if err != nil {
		log.Fatal(err)
	}
	defer rows.Close()

	var books []Book
	var book Book
	for rows.Next() {
		err := rows.Scan(&book.Title)
	}
}
