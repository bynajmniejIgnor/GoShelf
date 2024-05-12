package sqlhandler

import (
	"database/sql"
	"encoding/json"
	"fmt"
	"log"
	"strconv"
	"strings"

	_ "github.com/mattn/go-sqlite3"
)

func connect(db_path string) *sql.DB {
	db, err := sql.Open("sqlite3", db_path)
	if err != nil {
		log.Fatal(err)
	}
	_, err = db.Exec("PRAGMA foreign_keys = ON;")
	if err != nil {
		log.Fatal(err)
	}
	return db
}

type Shelf struct {
	Name         string
	Shelf_id     int
	Books_stored int
}

type Book struct {
	Title, Subtitle, Authors string
}

func GetShelves(user_id string) (string, error) {
	db := connect("./sqlhandler/goshelf.db")
	defer db.Close()

	rows, err := db.Query("SELECT name, shelf_id, books_stored FROM shelves WHERE user_id = ?", user_id)
	if err != nil {
		log.Fatal(err)
	}
	defer rows.Close()

	var shelves []Shelf
	var shelf Shelf
	for rows.Next() {
		err := rows.Scan(&shelf.Name, &shelf.Shelf_id, &shelf.Books_stored)
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

func GetUserIdByAndroidId(android_id string) string {
	db := connect("./sqlhandler/goshelf.db")
	defer db.Close()

	row, err := db.Query("SELECT user_id FROM users WHERE android_id = ?", android_id)
	if err != nil {
		log.Fatal(err)
	}
	defer row.Close()

	var user_id sql.NullString

	for row.Next() {
		row.Scan(&user_id)
		if err != nil {
			log.Fatal(err)
		}

		if user_id.Valid {
			return user_id.String
		}
	}
	return ""
}

func SetAndroidId(user_id, android_id string) error {
	db := connect("./sqlhandler/goshelf.db")
	defer db.Close()
	cmd := `
		UPDATE users
		SET android_id = ?
		WHERE user_id = ?
	`
	_, err := db.Exec(cmd, android_id, user_id)
	if err != nil {
		log.Fatal(err)
		return err
	}
	return nil
}

func GetUserID(username, hash string) (string, error) {
	db := connect("./sqlhandler/goshelf.db")
	defer db.Close()

	row, err := db.Query("SELECT user_id FROM users WHERE username = ? AND password = ?", username, hash)
	if err != nil {
		log.Fatal(err)
	}
	defer row.Close()

	var user_id sql.NullInt32

	for row.Next() {
		err = row.Scan(&user_id)
		if err != nil {
			log.Fatal(err)
		}
		if user_id.Valid {
			return strconv.FormatInt(int64(user_id.Int32), 10), nil
		}
	}
	return "-1", fmt.Errorf("login or password incorrect")
}

func GetBooks(shelf_id string) (string, error) {
	db := connect("./sqlhandler/goshelf.db")
	defer db.Close()

	rows, err := db.Query("SELECT title, subtitle, authors FROM books WHERE shelf_id = ?", shelf_id)
	if err != nil {
		log.Fatal(err)
	}
	defer rows.Close()

	var books []Book
	var book Book
	var subtitle sql.NullString
	for rows.Next() {
		err := rows.Scan(&book.Title, &subtitle, &book.Authors)
		if err != nil {
			log.Fatal(err)
		}

		if subtitle.Valid {
			book.Subtitle = subtitle.String
		} else {
			book.Subtitle = ""
		}

		books = append(books, book)
	}

	jsonData, err := json.Marshal(books)
	if err != nil {
		log.Fatal(err)
	}

	return string(jsonData), nil
}

func AddShelf(name, user_id string) (string, error) {
	db := connect("./sqlhandler/goshelf.db")
	defer db.Close()
	cmd := `
		INSERT INTO shelves (name, user_id)
		VALUES (?, ?)
	`
	_, err := db.Exec(cmd, name, user_id)
	if err != nil {
		log.Fatal(err)
		return "", err
	}

	row, err := db.Query("SELECT shelf_id FROM shelves WHERE user_id = ? AND name = ?", user_id, name)
	if err != nil {
		log.Fatal(err)
	}
	defer row.Close()

	var shelf_id string
	for row.Next() {
		err := row.Scan(&shelf_id)
		if err != nil {
			log.Fatal(err)
		}
	}
	return shelf_id, nil
}

func DeleteShelf(shelf_id string) error {
	db := connect("./sqlhandler/goshelf.db")
	defer db.Close()
	cmd := `
		DELETE FROM shelves
		WHERE shelf_id = ?
	`
	_, err := db.Exec(cmd, shelf_id)
	if err != nil {
		log.Fatal(err)
		return err
	}
	return nil
}

func SearchShelf(user_id, name string) (string, error) {
	db := connect("./sqlhandler/goshelf.db")
	defer db.Close()

	rows, err := db.Query("SELECT name, shelf_id, books_stored FROM shelves WHERE user_id = ? AND name LIKE ?", user_id, fmt.Sprintf("%%%s%%", name))
	if err != nil {
		log.Fatal(err)
	}
	defer rows.Close()

	var shelves []Shelf
	var shelf Shelf
	for rows.Next() {
		err := rows.Scan(&shelf.Name, &shelf.Shelf_id, &shelf.Books_stored)
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

func AddBook(shelf_id, title, subtitle, authors string) (string, error) {
	tmp := strings.Trim(authors, "[")
	f_authors := strings.Trim(tmp, "]")

	db := connect("./sqlhandler/goshelf.db")
	defer db.Close()
	cmd := `
		INSERT INTO books (title, subtitle, authors, shelf_id)
		VALUES (?, ?, ?, ?)`

	result, err := db.Exec(cmd, title, subtitle, f_authors, shelf_id)
	if err != nil {
		log.Fatal(err)
		return "", nil
	}

	cmd = `
		UPDATE shelves
		SET books_stored = (SELECT books_stored FROM shelves WHERE shelf_id = ?) + 1`

	_, err = db.Exec(cmd, shelf_id)
	if err != nil {
		log.Fatal(err)
		return "", nil
	}

	rowid, _ := result.LastInsertId()
	return fmt.Sprint(rowid), nil
}

func BookSearch(query string) (string, error) {
	db := connect("./sqlhandler/goshelf.db")
	defer db.Close()
	f_query := fmt.Sprintf("%%%s%%", query)
	rows, err := db.Query("SELECT title, subtitle, authors FROM books WHERE title LIKE ? OR subtitle LIKE ? OR authors LIKE ?", f_query, f_query, f_query)
	if err != nil {
		log.Fatal(err)
	}

	var book Book
	var subtitle sql.NullString
	var books []Book

	for rows.Next() {
		err := rows.Scan(&book.Title, &subtitle, &book.Authors)
		if err != nil {
			log.Fatal(err)
		}

		if subtitle.Valid {
			book.Subtitle = subtitle.String
		} else {
			book.Subtitle = ""
		}
		books = append(books, book)
	}

	result, err := json.Marshal(books)
	if err != nil {
		log.Fatal(err)
	}

	return string(result), nil
}
