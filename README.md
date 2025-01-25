## Set up your environment
```
export DEEPSEEK_API_KEY=your_api_key
```

## Running the application
```
mvn clean spring-boot:run
```

Then call the API:
```
curl localhost:8080/generate/1
```

### Running with 1Password integration
To not have to manually set the environment variables, you can use 1Password integration to set
them at runtime.

```
op run --env-file="./secrets.env" -- mvn clean spring-boot:run
```