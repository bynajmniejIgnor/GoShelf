package api

import (
	"bytes"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
)

func loadFile(img_path string) string {
	file, err := os.ReadFile(img_path)

	if err != nil {
		fmt.Println("Error loading file", err)
		return ""
	}

	encoded := base64.StdEncoding.EncodeToString([]byte(file))
	return encoded
}

func formatPayload(encoded_img string) *bytes.Buffer {
	data := make(map[string]string)
	data["trim"] = "\n"
	data["base64"] = encoded_img

	jsonData, err := json.Marshal(data)

	if err != nil {
		fmt.Println("Error marshaling JSON", err)
		return nil
	}

	payload := bytes.NewBuffer(jsonData)

	return payload
}

func processImg(client *http.Client, encoded_img string) string {
	url := "http://localhost:8080/base64"

	payload := formatPayload(encoded_img)
	req, err := http.NewRequest("POST", url, payload)

	if err != nil {
		fmt.Println("Error creating POST request", err)
	}

	req.Header.Set("Content-Type", "application/json")

	resp, err := client.Do(req)

	if err != nil {
		fmt.Println("Error sending request", err)
		return ""
	}
	defer resp.Body.Close()

	reply, err := io.ReadAll(resp.Body)

	if err != nil {
		fmt.Println("Error reading response body", err)
		return ""
	}
	return string(reply)
}

func mapResponse(reply string) (map[string]string, error) {
	mappedData := make(map[string]string)
	err := json.Unmarshal([]byte(string(reply)), &mappedData)

	if err != nil {
		return nil, err
	}
	return mappedData, nil
}

func main() {
	client := &http.Client{}

	encoded_img := loadFile("test.png")
	resp := processImg(client, encoded_img)
	data, err := mapResponse(resp)

	if err != nil {
		fmt.Println("Error mapping response:", err)
	}

	fmt.Println("OCRed text:", data["result"])
}
