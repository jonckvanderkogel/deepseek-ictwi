## Set up your environment
```
export DEEPSEEK_API_KEY=your_api_key
```

## Running the application
```
mvn clean spring-boot:run
```

Then call the API. By default the most similar examples are used to send along to DeepSeek. You can also use all
examples to send along, in that case pass along use-all-examples=true as request parameter.
```
curl localhost:8080/generate/1
curl localhost:8080/generate/1?use-all-examples=true
```

To pretty print the output of the API call, call the service like this:
```
curl -s localhost:8080/generate/1 | jq -r '.choices[0].message.content'
curl -s 'localhost:8080/generate/1?use-all-examples=true' | jq -r '.choices[0].message.content'
```

### Running with 1Password integration
To not have to manually set the environment variables, you can use 1Password integration to set
them at runtime.

```
op run --env-file="./secrets.env" -- mvn clean spring-boot:run
```