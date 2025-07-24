# Mini-Market-Order Service

### Setup
```bash
docker-compose up --build
```

### H2 Console
Visit: http://localhost:8080/h2-console  
JDBC URL: `jdbc:h2:mem:testdb`  
User: `sa`, Password: (leave empty)

### Sample curl
```bash
curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" \
 -d '{"accountId":"account12345", "symbol":"AAPL", "side":"BUY", "quantity":10}'

curl http://localhost:8080/orders?accountId=account12345
```
