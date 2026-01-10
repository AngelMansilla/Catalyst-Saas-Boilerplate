#!/bin/bash

# Catalyst LocalStack S3 Initialization Script
# This script creates S3 buckets for the application

echo "Initializing LocalStack S3 buckets..."

# Wait for LocalStack to be ready
sleep 5

# Set AWS CLI to use LocalStack
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1

# Create S3 buckets
echo "Creating S3 bucket: catalyst-uploads"
awslocal s3 mb s3://catalyst-uploads

echo "Creating S3 bucket: catalyst-documents"
awslocal s3 mb s3://catalyst-documents

echo "Creating S3 bucket: catalyst-avatars"
awslocal s3 mb s3://catalyst-avatars

echo "Creating S3 bucket: catalyst-backups"
awslocal s3 mb s3://catalyst-backups

# Set bucket policies for public read on avatars (for development)
echo "Setting bucket policy for catalyst-avatars"
awslocal s3api put-bucket-policy --bucket catalyst-avatars --policy '{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::catalyst-avatars/*"
    }
  ]
}'

# Enable CORS for all buckets
for bucket in catalyst-uploads catalyst-documents catalyst-avatars catalyst-backups; do
  echo "Enabling CORS for bucket: $bucket"
  awslocal s3api put-bucket-cors --bucket $bucket --cors-configuration '{
    "CORSRules": [
      {
        "AllowedOrigins": ["*"],
        "AllowedMethods": ["GET", "PUT", "POST", "DELETE", "HEAD"],
        "AllowedHeaders": ["*"],
        "ExposeHeaders": ["ETag"],
        "MaxAgeSeconds": 3000
      }
    ]
  }'
done

# List created buckets
echo "S3 buckets created successfully:"
awslocal s3 ls

echo "LocalStack S3 initialization complete!"


