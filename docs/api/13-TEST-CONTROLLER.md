# Test Controller Documentation

---

## 1. Ping Endpoint

**Endpoint:** `GET /ping`

**Access:** Public

**Deskripsi:** Endpoint untuk testing konektivitas dan kesehatan API server.

### Request

- Method: GET
- No parameters
- No authentication required

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "pong",
  "result": "ping pong"
}
```

---

## Usage

Endpoint ini digunakan untuk:

1. **Health Check:** Memastikan API server berjalan
2. **Connectivity Test:** Menguji koneksi dan response time
3. **Load Balancer Health:** Untuk health check routing
4. **Development Testing:** Quick API test saat development

### cURL Example

```bash
curl -X GET http://localhost:8080/ping
```

### JavaScript Fetch Example

```javascript
fetch('http://localhost:8080/ping')
  .then(response => response.json())
  .then(data => console.log(data))
  .catch(error => console.error('Error:', error));
```

### Python Requests Example

```python
import requests

response = requests.get('http://localhost:8080/ping')
print(response.json())
```

---

## Response Format

Endpoint mengembalikan `WebResponse` dengan struktur:

```json
{
  "success": boolean,
  "message": string,
  "result": string
}
```

## Performance

- **Response Time:** < 1ms (typically)
- **Status Code:** 200 OK
- **No Database Queries:** Direct response, no I/O operations

## Authentication

- **Ping Endpoint:** Public (no authentication required)

## Notes

- Endpoint tidak melakukan database queries
- Ideal untuk monitoring dan health checks
- Lightweight dan fast
- Beroperasi setiap saat tanpa kondisi tertentu
