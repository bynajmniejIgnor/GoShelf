package server

import (
	"encoding/base64"
	"fmt"
	"math/rand"
	"os"
	"os/exec"
	"time"

	"path/filepath"

	"github.com/labstack/echo/v4"
)

func SaveFile(encoded string) (string, error) {
	file_name := RandomString(8)
	file_path := "server/images/" + file_name

	decoded, err := base64.StdEncoding.DecodeString(encoded)
	if err != nil {
		return "", err
	}

	err = os.WriteFile(file_path, decoded, 0644)

	if err != nil {
		return "", err
	}
	return "images/" + file_name, nil
}

const charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

func RandomString(length int) string {
	rand.Seed(time.Now().UnixNano())
	randomString := make([]byte, length)
	for i := range randomString {
		randomString[i] = charset[rand.Intn(len(charset))]
	}

	return string(randomString)
}

func removeInputOutputFiles(input string, output string) error {
	if err1, err2 := os.Remove(input), os.Remove(output); err1 != nil || err2 != nil {
		return fmt.Errorf("failed to remove tmp files")
	}
	return nil
}

func tesseract(file string, lang string) (string, error) {
	cwd, _ := os.Getwd()
	input_path := filepath.Join(cwd, "server", file)
	ocr_path := filepath.Join(cwd, "server/ocr", RandomString(8))
	_, err := exec.Command("tesseract", input_path, ocr_path, "-l", lang).Output()
	ocr_path += ".txt" //Tesseract by default appends ".txt" at the end of output file

	if err != nil {
		return "Tesseract failed, probably requested language is not supported", err
	}

	text, err := os.ReadFile(ocr_path)

	if err != nil {
		return "Reading file failed", err
	}

	defer removeInputOutputFiles(input_path, ocr_path)
	return string(text), nil
}

type ImageData struct {
	Base64 string `json:"base64"`
	Lang   string `json:"lang"`
	Auth   string `json:"auth_token"`
}

func ProcessImage(c echo.Context) (string, error) {
	req := new(ImageData)
	if err := c.Bind(req); err != nil {
		return "", fmt.Errorf("bad request")
	}

	if req.Auth == "" {
		return "", fmt.Errorf("authorization failed")
	}

	img, err := SaveFile(req.Base64)

	if err != nil {
		return "Failed to save file", err
	}

	ocr, err := tesseract(img, req.Lang)

	if err != nil {
		return ocr, err
	}

	return ocr, nil
}
