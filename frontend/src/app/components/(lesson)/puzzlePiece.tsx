import { Card, CardBody } from "@nextui-org/react";
import { Popover, PopoverTrigger, PopoverContent } from "@nextui-org/popover";
import { Button } from "@nextui-org/button";

interface IPuzzlePiece {
  id: number;
}

export default function PuzzlePiece({ id }: IPuzzlePiece) {
  return (
    <div>
      <Popover placement="right">
        <PopoverTrigger>
          <Card className="w-24 h-24">
            <CardBody className="items-center justify-center">
              <p>{id}</p>
            </CardBody>
          </Card>
        </PopoverTrigger>
        <PopoverContent>
          <div className="px-1 py-2">
            <div className="text-small font-bold">Popover Content</div>
            <div className="text-tiny">This is the popover content</div>
            <div className="flex justify-center pt-2">
              <Button color="primary">Start</Button>
            </div>
          </div>
        </PopoverContent>
      </Popover>
    </div>
  );
}
