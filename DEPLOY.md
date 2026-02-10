# Production Deployment Instructions

## Prerequisites
- Docker installed on the target machine.
- The exported image file `update-steps.tar` (Includes all frontend resources for offline use).

## Steps

1. **Load the Docker Image**
   Transfer `update-steps.tar` to the server and run:
   ```bash
   docker load -i update-steps.tar
   ```

2. **Prepare Data Directory**
   Create a directory on the host machine to store persistent data (database and uploads):
   ```bash
   mkdir -p /data/update-steps-data
   ```
   *Note: You can choose any directory on the host, just update the path in the run command below.*

3. **Run the Container**
   Execute the following command to start the application:
   ```bash
   docker run -d \
     -p 8080:8080 \
     -e SPRING_PROFILES_ACTIVE=docker \
     -v /data/update-steps-data:/data \
     --name update-steps \
     --restart always \
     update-steps:latest
   ```

4. **Verify Deployment**
   Check if the container is running:
   ```bash
   docker ps
   ```
   Access the application at `http://<server-ip>:8080`.

## Troubleshooting
View application logs:
```bash
docker logs -f update-steps
```
