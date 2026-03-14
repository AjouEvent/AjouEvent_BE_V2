import React, { useState } from "react";
import styled from "styled-components";

const EventUpload = () => {
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [images, setImages] = useState([]);
  const [imagePreviews, setImagePreviews] = useState([]);

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      const response = await fetch(
        `${process.env.REACT_APP_BE_URL}/api/file/multiple-presigned-urls?prefix=${title}&fileCount=${images.length}`,
        {
          method: "POST",
        }
      );

      if (!response.ok) {
        throw new Error("Failed to fetch presigned URLs");
      }

      const data = await response.json();
      console.log("Success:", data);

      await Promise.all(
        images.map(async (image, index) => {
          const presignedUrl = data[index].url;

          const uploadResponse = await fetch(presignedUrl, {
            method: "PUT",
            headers: {
              "Content-Type": image.type,
            },
            body: image,
          });

          if (!uploadResponse.ok) {
            throw new Error(`Failed to upload image ${index + 1}`);
          }

          console.log(`Image ${index + 1} uploaded successfully`);
        })
      );

      const urls = data.map((item) => item.url.split("?")[0]);
    } catch (error) {
      console.error("Error:", error);
    }
  };

  const handleImageChange = (event) => {
    const files = Array.from(event.target.files);
    setImages(files);

    const previews = files.map((file) => URL.createObjectURL(file));
    setImagePreviews(previews);
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <label>
          Title:
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
        </label>
      </div>
      <div>
        <label>
          Content:
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
          ></textarea>
        </label>
      </div>
      <div>
        <label>
          Images:
          <input
            type="file"
            accept="image/*"
            multiple
            onChange={handleImageChange}
          />
        </label>
      </div>
      <div>
        {imagePreviews.map((preview, index) => (
          <img
            key={index}
            src={preview}
            alt={`Preview ${index}`}
            style={{ width: "100px", height: "auto", margin: "5px" }}
          />
        ))}
      </div>
      <button type="submit">Submit</button>
    </form>
  );
};

export default EventUpload;
