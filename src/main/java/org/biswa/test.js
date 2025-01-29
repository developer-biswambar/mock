import React, { useState, useEffect } from "react";
import FileTree from "@nosferatu500/react-file-tree";
import "@nosferatu500/react-file-tree/dist/style.css";

const FileSystemViewer = () => {
  const [fileData, setFileData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch("https://api.example.com/files") // Replace with your API endpoint
      .then((response) => {
        if (!response.ok) {
          throw new Error("Network response was not ok");
        }
        return response.json();
      })
      .then((data) => {
        // Ensure only two levels
        const formattedData = data.map((item) => ({
          id: item.id,
          name: item.name,
          isFolder: item.type === "folder",
          children: item.children || [],
        }));
        setFileData(formattedData);
        setLoading(false);
      })
      .catch((error) => {
        setError(error.message);
        setLoading(false);
      });
  }, []);

  if (loading) return <p>Loading...</p>;
  if (error) return <p>Error: {error}</p>;

  return (
    <div>
      <h2>File System Viewer</h2>
      <FileTree files={fileData} />
    </div>
  );
};

export default FileSystemViewer;
