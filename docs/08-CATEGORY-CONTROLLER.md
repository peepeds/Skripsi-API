# Category Controller

## Endpoint

### GET /category

Mengambil semua data kategori. Parameter opsional:
- `IncludeSubCategories` (default: 1)
    - Jika 1 (atau true): Kategori beserta sub-kategori.
    - Jika 0 (atau false): Hanya kategori utama.

#### Contoh Request
```
GET /category?IncludeSubCategories=1
```

#### Contoh Response
```json
{
  "success": true,
  "message": "Successfully Get Categories data",
  "result": [
    {
      "categoryId": 1,
      "categoryName": "Technology",
      "subCategories": [
        { "subCategoryId": 1, "subCategoryName": "Software Development" },
        { "subCategoryId": 2, "subCategoryName": "Cloud Computing" }
      ]
    },
    ...
  ]
}
```

## Catatan
- Endpoint ini tidak di-protect (public access).
- Logic pemrosesan ada di service, controller hanya delegasi.
- Response menggunakan `toResponse` mirip CompanyService.

