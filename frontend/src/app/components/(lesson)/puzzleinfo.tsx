interface PuzzleInfoProps {
    id: number;
  }
  
  export default function PuzzleInfo({ id }: PuzzleInfoProps) {
    return (
      <div className="bg-white p-4 rounded shadow">
        <h1 className="text-xl font-bold">퍼즐 조각 정보</h1>
        <p>{id}번 퍼즐 조각에 대한 정보</p>
        {/* 추가적인 퍼즐 조각 정보를 여기에 추가 */}
      </div>
    );
  }
  