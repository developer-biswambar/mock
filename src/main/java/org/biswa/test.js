import React from "react";
import { useQuery, useMutation } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Table, TableHeader, TableRow, TableCell } from "@/components/ui/table";
import { Upload, RefreshCw, FileText } from "lucide-react";

const FileList = () => {
  // Fetch file list from the backend
  const { data: files, isLoading, isError, refetch } = useQuery(["files"], async () => {
    const response = await fetch("/api/files");
    if (!response.ok) throw new Error("Failed to fetch files");
    return response.json();
  });

  // Replace file mutation
  const replaceFileMutation = useMutation(async ({ id, file }) => {
    const formData = new FormData();
    formData.append("file", file);
    const response = await fetch(`/api/files/${id}/replace`, {
      method: "PUT",
      body: formData,
    });
    if (!response.ok) throw new Error("Failed to replace file");
  });

  // Sign file mutation
  const signFileMutation = useMutation(async (id) => {
    const response = await fetch(`/api/files/${id}/sign`, { method: "POST" });
    if (!response.ok) throw new Error("Failed to sign file");
  });

  if (isLoading) return <p>Loading...</p>;
  if (isError) return <p>Error loading files!</p>;

  return (
    <div>
      <h1 className="text-xl font-bold mb-4">File List</h1>
      <Table>
        <TableHeader>
          <TableRow>
            <TableCell>File Name</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Last Signed</TableCell>
            <TableCell>Actions</TableCell>
          </TableRow>
        </TableHeader>
        <tbody>
          {files.map((file) => (
            <TableRow key={file.id}>
              <TableCell>
                <FileText className="mr-2" /> {file.name}
              </TableCell>
              <TableCell>{file.signed ? "Signed" : "Unsigned"}</TableCell>
              <TableCell>
                {file.signedAt ? new Date(file.signedAt).toLocaleString() : "N/A"}
              </TableCell>
              <TableCell>
                <Button
                  onClick={() => signFileMutation.mutate(file.id)}
                  className="mr-2"
                >
                  Sign
                </Button>
                <Button
                  onClick={() => {
                    const fileInput = document.createElement("input");
                    fileInput.type = "file";
                    fileInput.onchange = (e) =>
                      replaceFileMutation.mutate({ id: file.id, file: e.target.files[0] });
                    fileInput.click();
                  }}
                >
                  Replace
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </tbody>
      </Table>
      <Button className="mt-4" onClick={refetch}>
        <RefreshCw className="mr-2" /> Refresh
      </Button>
    </div>
  );
};

export default FileList;
