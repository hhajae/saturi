"use client";

import {
  Table,
  TableBody,
  TableContainer,
  Paper,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Typography,
  TableRow,
  TableCell,
  Button,
  Checkbox,
  checkboxClasses,
  List,
  ListSubheader,
  ListItemText,
  ListItem,
  Box,
  TablePagination,
  TextField,
  Container,
  Radio,
  RadioGroup,
  FormControlLabel,
  FormControl,
} from "@mui/material";
import api from "@/lib/axios";
import { useRouter } from "next/navigation";
import React, { useEffect, useState } from "react";
import useTableSort from "@/hooks/useTableSort";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import SortableTableHead from "@/components/admin/admin-form/SortableTableHead";
import { styled } from "@mui/material/styles";
import {getFormattedLocationId, parseDate} from "@/utils/utils";

interface QuizProps {
  quizId: number;
  locationId: number;
  question: string;
  isObjective: boolean;
  creationDt: string;
  choiceList?: Choice[];
}

interface Choice {
  choiceId: number;
  content: string;
  isAnswer: boolean;
}

type HeadCell = {
  id: keyof QuizProps | "actions";
  label: string;
};

const headCells: HeadCell[] = [
  { id: "quizId", label: "Id" },
  { id: "locationId", label: "지역" },
  { id: "isObjective", label: "타입" },
  { id: "question", label: "문제" },
  { id: "creationDt", label: "생성일" },
  { id: "actions", label: "" }
];

const CustomCheckbox = styled(Checkbox)(({ theme }) => ({
  [`&.${checkboxClasses.root}`]: {
    color: theme.palette.primary.main,
    "&.Mui-checked": {
      color: theme.palette.primary.dark
    },
    "&.Mui-disabled": {
      color: theme.palette.primary.light
    }
  },
  [`& .${checkboxClasses.disabled}`]: {
    color: theme.palette.primary.light
  }
}));

export default function App() {
  const router = useRouter();
  const [items, setItems] = useState<QuizProps[]>([]);
  const { rows, order, orderBy, onRequestSort } = useTableSort<QuizProps>(items, "quizId");
  const [filters, setFilters] = useState({
    quizId: "",
    locationId: "",
    question: "",
    isObjective: ""
  });

  // Pagination 관련 상태
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(5);

  // 현재 페이지에 표시할 행 계산
  const displayedRows = rows.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  // 페이지 변경 핸들러
  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  // 페이지 당 행 수 변경 핸들러
  function handleChangeRowsPerPage(event: React.ChangeEvent<HTMLInputElement>) {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  }

  function fetchDetail(quizId: number) {
    api.get<QuizProps>(`/admin/game/quiz/${quizId}`).then((response) => {
      const updatedItems = items.map((item) =>
        item.quizId === quizId ? { ...item, choiceList: response.data.choiceList } : item
      );
      setItems(updatedItems);
    });
  }

  function handleEdit(quizId: number) {
    router.push(`/admin/quiz/edit/${quizId}`);
  }

  function handleDelete(quizId: number) {
    api.delete(`/admin/game/quiz/${quizId}`).then(() => {
      alert("삭제되었습니다.");
      setItems(items.filter((item) => item.quizId !== quizId));
    });
  }

  function fetchReports() {
    const queryParams = new URLSearchParams(
      Object.entries(filters).filter(([_, value]) => value !== '')
    ).toString();
    api.get(`/admin/game/quiz?${queryParams}`)
      .then((response) => {
        setItems(response.data);
        setPage(0)
      });
  }

  useEffect(() => {
    fetchReports();
  }, []);

  function handleFilterChange(event: React.ChangeEvent<HTMLInputElement>) {
    setFilters({ ...filters, [event.target.name]: event.target.value });
  }

  function handleRadioChange(event: React.ChangeEvent<HTMLInputElement>) {
    setFilters({ ...filters, isObjective: event.target.value });
  }

  return (
    <Container>
      <Typography component="h1" variant="h4" sx={{display: 'flex', justifyContent: "center", mb:3,}}>
          퀴즈 조회
        </Typography>

      <Box sx={{ display: 'flex', gap: 2, mb: 2, alignItems:"center", justifyContent:"right"}}>
        <TextField
          name="quizId"
          label="퀴즈 Id"
          value={filters.quizId}
          onChange={handleFilterChange}
        />
        <TextField
          name="locationId"
          label="지역 Id"
          value={filters.locationId}
          onChange={handleFilterChange}
        />
        <TextField
          name="question"
          label="문제"
          value={filters.question}
          onChange={handleFilterChange}
        />
        <FormControl>
          <RadioGroup
            row
            name="isObjective"
            value={filters.isObjective}
            onChange={handleRadioChange}
          >
            <FormControlLabel value="" control={<Radio />} label="모두" />
            <FormControlLabel value="true" control={<Radio />} label="객관식" />
            <FormControlLabel value="false" control={<Radio />} label="주관식" />
          </RadioGroup>
        </FormControl>
        <Button variant="contained" onClick={fetchReports}>검색</Button>
      </Box>
      <TableContainer component={Paper}>
        <Table>
          <SortableTableHead
            order={order}
            orderBy={orderBy}
            onRequestSort={onRequestSort}
            headCells={headCells}
          />
          <TableBody>
            {displayedRows.map((row) => (
              <TableRow key={row.quizId}>
                <TableCell className="w-1/12">{row.quizId}</TableCell>
                <TableCell className="w-1/12">{getFormattedLocationId(row.locationId)}</TableCell>
                <TableCell className="w-1/12">{row.isObjective ? "객관식" : "주관식"}</TableCell>
                <TableCell className="w-5/12">
                  <Accordion>
                    <AccordionSummary
                      expandIcon={<ExpandMoreIcon />}
                      onClick={() => fetchDetail(row.quizId)}
                    >
                      <Typography>{row.question}</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <List
                        aria-labelledby="nested-list-subheader"
                        subheader={
                          <ListSubheader component="div" id="nested-list-subheader">
                            선택지
                          </ListSubheader>
                        }
                      >
                        {row.choiceList ? (
                          row.choiceList.map((choice) => (
                            <ListItem disablePadding key={choice.choiceId}>
                              <CustomCheckbox disabled checked={choice.isAnswer} />
                              <ListItemText primary={`${choice.choiceId}번 : ${choice.content}`} />
                            </ListItem>
                          ))
                        ) : (
                          <Typography>로딩 중...</Typography>
                        )}
                      </List>
                      <Box
                        sx={{
                          display: "flex",
                          justifyContent: "center",
                          gap: 1,
                          mt: 2
                        }}
                      >
                      </Box>
                    </AccordionDetails>
                  </Accordion>
                </TableCell>
                <TableCell className="w-2/12">{new Date(row.creationDt).toLocaleString()}</TableCell>
                <TableCell className="w-2/12">
                  <Button
                    variant="contained"
                    color="success"
                    onClick={() => handleEdit(row.quizId)}
                    sx={{
                      mr: 1
                    }}
                  >
                    수정
                  </Button>
                  <Button variant="contained" onClick={() => handleDelete(row.quizId)}>
                    삭제
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <TablePagination
          rowsPerPageOptions={[5, 10, 25]}
          component="div"
          count={rows.length}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage}
          labelRowsPerPage="페이지 당 항목 수: "
        />
      </TableContainer>
    </Container>
  );
}
