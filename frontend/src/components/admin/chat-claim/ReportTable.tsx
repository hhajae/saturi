import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Button } from '@mui/material';
import SortableTableHead from "@/components/SortableTableHead";
import useTableSort from "@/hooks/useTableSort";

interface UserReport {
  chatClaimId: number;
  gameLogId: number;
  userId: number;
  roomId: number;
  quizId: number;
  actions: string
}

interface ReportTableProps {
  reports: UserReport[];
  onDelete: (chatClaimId: number) => void;
  onBan: (userId: number, chatClaimId: number) => void;
}

type HeadCell = {
  id: keyof UserReport;
  label: string;
};

const headCells: HeadCell[] = [
  { id: "chatClaimId", label: "Id" },
  { id: "gameLogId", label: "지역" },
  { id: "userId", label: "생성일" },
  { id: "roomId", label: "문제 타입" },
  { id: "quizId", label: "문제" },
  { id: "actions", label: "" }
];

const ReportTable: React.FC<ReportTableProps> = ({ reports, onDelete, onBan }) => {
  const { rows, order, orderBy, onRequestSort } = useTableSort<UserReport>(reports, "chatClaimId");

  return (
    <TableContainer component={Paper}>
      <Table>
        <SortableTableHead
          order={order}
          orderBy={orderBy}
          onRequestSort={onRequestSort}
          headCells={headCells}
        />
        {/* <TableHead> */}
        {/*   <TableRow> */}
        {/*     <TableCell>Chat Claim ID</TableCell> */}
        {/*     <TableCell>Game Log ID</TableCell> */}
        {/*     <TableCell>User ID</TableCell> */}
        {/*     <TableCell>Room ID</TableCell> */}
        {/*     <TableCell>Quiz ID</TableCell> */}
        {/*     <TableCell>Actions</TableCell> */}
        {/*   </TableRow> */}
        {/* </TableHead> */}
        <TableBody>
          {rows.map((report) => (
            <TableRow key={report.chatClaimId}>
              <TableCell>{report.chatClaimId}</TableCell>
              <TableCell>{report.gameLogId}</TableCell>
              <TableCell>{report.userId}</TableCell>
              <TableCell>{report.roomId}</TableCell>
              <TableCell>{report.quizId}</TableCell>
              <TableCell>
                <Button onClick={() => onDelete(report.chatClaimId)}>Delete</Button>
                <Button onClick={() => onBan(report.userId, report.chatClaimId)}>Ban</Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default ReportTable;