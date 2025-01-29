import React, { useState, useEffect } from "react";
import SortableTree from "react-sortable-tree";
import "react-sortable-tree/style.css";

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
          title: item.name,
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
      <div style={{ height: 400 }}>
        <SortableTree
          treeData={fileData}
          onChange={setFileData}
        />
      </div>
    </div>
  );
};

export default FileSystemViewer;
