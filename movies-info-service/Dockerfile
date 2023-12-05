# Use an official MongoDB image as a base image
FROM mongodb/mongodb-community-server:latest

# Set environment variables
#ENV MONGO_INITDB_ROOT_USERNAME=admin
#ENV MONGO_INITDB_ROOT_PASSWORD=mypassword

# Create a directory to store data
RUN mkdir -p /data/db

# Expose the default MongoDB port
EXPOSE 27017

# Start MongoDB when the container starts
CMD ["mongod"]