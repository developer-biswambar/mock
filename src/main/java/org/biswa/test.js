import React, { useState, useEffect } from "react";
import { Tree, NodeModel } from "@minoru/react-dnd-treeview";
import "@minoru/react-dnd-treeview/dist/style.css";

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
          parent: item.parentId || 0,
          text: item.name,
          droppable: item.type === "folder",
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
      <Tree
        tree={fileData}
        rootId={0}
        render={(node) => <span>{node.text}</span>}
        enableDragAndDrop={false}
      />
    </div>
  );
};

export default FileSystemViewer;
