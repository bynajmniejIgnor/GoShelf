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

func tesseract(file string) (string, error) {
	cwd, _ := os.Getwd()
	ocr_path := "server/ocr/" + RandomString(8)
	_, err := exec.Command("tesseract", filepath.Join(cwd, "server", file), filepath.Join(cwd, ocr_path), "-l", "pol").Output()

	if err != nil {
		return "Tesseract failed", err
	}

	text, err := os.ReadFile(filepath.Join(cwd, ocr_path) + ".txt")

	if err != nil {
		return "Reading file failed", err
	}

	return string(text), nil
}

type ImageData struct {
	Base64 string `json:"base64"`
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

	ocr, err := tesseract(img)

	if err != nil {
		return ocr, err
	}

	return ocr, nil
}
