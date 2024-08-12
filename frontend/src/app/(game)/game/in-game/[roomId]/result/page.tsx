"use client";

import { Box, Container, Typography, Card, LinearProgress, Button } from "@mui/material";
import {useEffect, useState} from "react";
import api from "@/lib/axios";
import {RoomIdProps} from "@/utils/props";
import FirstResult from "@/components/game/result/firstResult";
import SecondResult from "@/components/game/result/secondResult";

interface RankProps {
  rank : number
  birdId: number
  nickName: string
  ansCount: number
  earnedExp: number
  exp: number
  user: boolean
}

export default function Result({ params: { roomId } }: RoomIdProps) {
    const [currentStep, setCurrentStep] = useState(1);
    const [ ranks, setRanks ] = useState<RankProps[]>([])
    const [ myRank, setMyRank ] = useState<RankProps>({
      rank: 0,
      birdId: 0,
      nickName: '',
      ansCount: 0,
      earnedExp: 0,
      exp: 0,
      user: false
    })

    const nextstep = () => {
        if (currentStep === 1){
            setCurrentStep(currentStep + 1);
        }        
      };

    useEffect(() => {
        api.post("/game/result", {
            roomId
        })
          .then(response => {
              setRanks(response.data)
              setMyRank(response.data.filter((rank: RankProps) => rank.user)[0])
            })
    }, []);

    return(
        <Container
            maxWidth="lg" 
            sx={{                
                height:"90vh",
                minHeight:"700px",
        }}>
            <FirstResult
                currentStep = {currentStep}
                nextstep = {nextstep}
                ranks = {ranks}
            />
            <SecondResult
                currentStep = {currentStep}
                myRank = {myRank}
            />
        </Container>
    )
}