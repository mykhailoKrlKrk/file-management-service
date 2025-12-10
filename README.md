# file-management-service

![Java Badge](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring Boot Badge](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-bae&logo=spring-boot) ![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=Swagger&logoColor=white)

A Spring Boot service for uploading, validating, converting, storing, indexing, and retrieving structured files.  
The service currently supports XML → JSON transformation and provides fast search functionality based on **customer**, **type**, and **date** using filesystem indexing.

---

## Features

- Upload XML files with strict filename validation  
- Convert XML to JSON  
- Store processed JSON files in a structured filesystem  
- Prevent duplicate uploads (upload endpoint)  
- Replace existing files (update endpoint)  
- Delete files and associated index entries  
- Retrieve file content by name  
- Indexed lookup by:
  - customer  
  - type  
  - date  

Indexes are implemented using filesystem symbolic links for O(1) directory lookup.

---

## Business Processes

### 1. File Upload

1. Validate filename structure:  
   `<customer>_<type>_<yyyy-MM-dd>.xml`
2. Parse XML and convert to JSON.
3. Save JSON file under:
   `storage/files/<customer>_<type>_<yyyy-MM-dd>.json`
4. Create symlink indexes in:
   - `storage/index-by-customer/<customer>/<file>.json → ../../files/<file>.json`
   - `storage/index-by-type/<type>/<file>.json → ../../files/<file>.json`
   - `storage/index-by-date/<yyyy-MM-dd>/<file>.json → ../../files/<file>.json`
5. Return `201 Created`.

---

### 2. File Update

- Behaves like upload, except existing files are overwritten.  
- Symlink index entries are recreated.  
- Returns `202 Accepted`.

---

### 3. File Delete

- Deletes the main JSON file under `storage/files/`.  
- Removes symlink references from all index directories.  
- Returns `204 No Content`.

---

### 4. File Retrieval

- Locate JSON file based on the provided filename.  
- Return file content as a `Resource`.  
---

### 5. Indexed Search

#### By Date
`storage/index-by-date/<yyyy-MM-dd>/`

#### By Customer
`storage/index-by-customer/<customer>/`

#### By Type
`storage/index-by-type/<type>/`

---

## Filesystem Structure

```
storage/
  files/
    acme_report_2025-12-09.json
    globex_summary_2025-12-10.json

  index-by-customer/
    acme/
      acme_report_2025-12-09.json → ../../files/acme_report_2025-12-09.json

  index-by-type/
    report/
      acme_report_2025-12-09.json → ../../files/acme_report_2025-12-09.json

  index-by-date/
    2025-12-09/
      acme_report_2025-12-09.json → ../../files/acme_report_2025-12-09.json
```

---

## Technologies Used

- Java 17+  
- Spring Boot  
- Jackson (ObjectMapper, XmlMapper)  
- Java NIO symbolic links  
- Custom filename validation  
- Checkstyle formatting rules  

---

## Filename Format

`<customer>_<type>_<yyyy-MM-dd>.xml`

### Regex

`^[a-zA-Z0-9]+_[a-zA-Z0-9]+_\d{4}-\d{2}-\d{2}\.xml$`

### Segments

| Segment   | Description |
|----------|-------------|
| customer | Client/company identifier |
| type     | Document/category type |
| date     | ISO date `yyyy-MM-dd` |

---

## REST API Endpoints

- `POST /api/v1/files` — upload  
- `PUT /api/v1/files` — update  
- `DELETE /api/v1/files/{fileName}` — delete  
- `GET /api/v1/files/{fileName}` — get file  
- `GET /api/v1/files/find-by-date/{yyyy-MM-dd}` — search by date  
- `GET /api/v1/files/find-by-customer/{customer}` — search by customer  
- `GET /api/v1/files/find-by-type/{type}` — search by type  

---
