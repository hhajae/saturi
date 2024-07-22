import { useRouter } from "next/navigation";

interface PuzzleProps {
  id: string;
}

// async function getPuzzle(id: string) {
//   const response = await fetch(`문제그룹 받아오기/${id}`);
//   return response.json();
// }

export default function Puzzle({ id }: PuzzleProps) {
  // const pieces = await getPuzzle(id);
  const pieces = [1, 2, 3, 4, 5, 6, 7, 8, 9];
  const router = useRouter();
  const onClick = () => {
    router.push(`/lesson/${id}`);
  };
  return (
    <div>
      {pieces.map(piece => (
        <img key={piece} src={`${piece}`} alt={`${piece}`} onClick={onClick} />
      ))}
    </div>
    // <h1>nothing</h1>
  );
}
