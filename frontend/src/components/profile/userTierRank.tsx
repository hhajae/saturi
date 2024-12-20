import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import { Paper, Box, Typography, CircularProgress } from "@mui/material";
import api from '@/lib/axios';
import { FaCrown } from 'react-icons/fa';

const tierImages = {
  stone: '/tier/stone1.gif',
  bronze: '/tier/bronze1.gif',
  silver: '/tier/silver1.gif',
  gold: '/tier/gold1.gif',
  sapphire: '/tier/sapphire1.gif',
  platinum: '/tier/platinum1.gif',
  diamond: '/tier/diamond1.gif'
};

type TierKey = keyof typeof tierImages;

const tierThresholds = {
  stone: 0,
  bronze: 1,
  silver: 100,
  gold: 500,
  platinum: 1500,
  sapphire: 3000,
  diamond: 5000
};

const getTierFromExp = (exp: number): TierKey => {
  if (exp === 0) return 'stone';
  if (exp < 100) return 'bronze';
  if (exp < 500) return 'silver';
  if (exp < 1500) return 'gold';
  if (exp < 3000) return 'platinum';
  if (exp < 5000) return 'sapphire';
  return 'diamond';
};

const getNextTier = (currentTier: TierKey): TierKey | null => {
  const tiers = Object.keys(tierThresholds) as TierKey[];
  const currentIndex = tiers.indexOf(currentTier);
  return currentIndex < tiers.length - 1 ? tiers[currentIndex + 1] : null;
};

const getExpToNextTier = (currentExp: number): number | null => {
  const currentTier = getTierFromExp(currentExp);
  const nextTier = getNextTier(currentTier);
  if (nextTier) {
    return tierThresholds[nextTier] - currentExp;
  }
  return null;
};

const formatTierName = (tier: TierKey): string => {
  return tier.charAt(0).toUpperCase() + tier.slice(1) + ' Tier';
};

interface UserExpInfo {
  currentExp: number;
  userRank: number | null;
}

interface UserTierRankProps {
  layout?: 'vertical' | 'horizontal';
}

const UserTierRank: React.FC<UserTierRankProps> = ({ layout = 'vertical' }) => {
  const [userExpInfo, setUserExpInfo] = useState<UserExpInfo | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [imageLoaded, setImageLoaded] = useState(false);

  useEffect(() => {
    const fetchUserExpInfo = async () => {
      try {
        setIsLoading(true);
        const accessToken = sessionStorage.getItem('accessToken');
        if (!accessToken) {
          throw new Error('Access token not found');
        }

        const response = await api.get<{ userExpInfo: UserExpInfo }>('/user/auth/dashboard');
        setUserExpInfo(response.data.userExpInfo);

      } catch (error) {
        console.error('Failed to fetch user exp info:', error);
        // Set default values when data fetch fails
        setUserExpInfo({ currentExp: 0, userRank: null });
      } finally {
        setIsLoading(false);
      }
    };

    fetchUserExpInfo();
  }, []);

  useEffect(() => {
    setImageLoaded(false);
  }, [userExpInfo?.currentExp]);

  if (isLoading) {
    return <CircularProgress />;
  }

  const tierKey = getTierFromExp(userExpInfo?.currentExp || 0);
  const imageSrc = tierImages[tierKey];
  const tierName = formatTierName(tierKey);
  const expToNextTier = getExpToNextTier(userExpInfo?.currentExp || 0);
  const isMaxTier = (userExpInfo?.currentExp || 0) >= 5000;

  const isHorizontal = layout === 'horizontal';

  return (
    <Paper elevation={3}
    sx={{ 
      p: 2, 
      borderRadius: '16px',
      position: 'relative',
      height: isHorizontal ? 'auto' : '80%',
      display: 'flex',
      flexDirection: isHorizontal ? 'row' : 'column',
      alignItems: 'center',
      justifyContent: isHorizontal ? 'flex-start' : 'center',
      width: isHorizontal ? '100%' : 'auto', 
    }}>
      {!isHorizontal && (
        <Box sx={{ top: 16, left: 16, display: 'flex', alignItems: 'center', marginTop: 2 }}>
          <FaCrown style={{ color: 'gold', marginRight: 8, fontWeight: 'bold' }} />
          <Typography variant='body1' fontWeight="bold" >
            {userExpInfo?.userRank ? `전체 ${userExpInfo.userRank}위` : 'UnRanked'}
          </Typography>
        </Box>
      )}
      <Box sx={{ 
        marginTop: isHorizontal ? 0 : 1,
        marginRight: isHorizontal ? 2 : 0,
        position: 'relative',
        width: isHorizontal ? 60 : 120,
        height: isHorizontal ? 60 : 120,
      }}>
        {!imageLoaded && (
          <CircularProgress 
            size={isHorizontal ? 40 : 60}
            sx={{ 
              position: 'absolute', 
              top: '50%', 
              left: '50%', 
              transform: 'translate(-50%, -50%)' 
            }} 
          />
        )}
        <Image
          src={imageSrc}
          alt={`${tierKey} Tier`}
          width={isHorizontal ? 70 : 120}
          height={isHorizontal ? 70 : 120}
          onLoad={() => setImageLoaded(true)}
          style={{ opacity: imageLoaded ? 1 : 0, transition: 'opacity 0.3s' }}
        />
      </Box>
      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        {isHorizontal && (
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
            <FaCrown style={{ color: 'gold', marginRight: 8, fontWeight: 'bold' }} />
            <Typography variant="body1" fontWeight="bold" >
              {userExpInfo?.userRank ? `전체 ${userExpInfo.userRank}위` : 'UnRanked'}
            </Typography>
          </Box>
        )}
        <Typography variant={isHorizontal ? "body1" : "h6"} color="text.primary" sx={{ mt: isHorizontal ? 0 : 1 }}>
          {tierName}
        </Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: '100%' }}>
          <Typography variant={isHorizontal ? "body1" : "h6"} color="text.secondary" sx={{ mb: isHorizontal ? 0 : 1 }}>
            {`${userExpInfo?.currentExp || 0} EXP`}
          </Typography>
        </Box>
        <Box 
          sx={{
            marginBottom:"15px",
          }}>
          {!isHorizontal && (
              <Typography variant="body1" color="text.secondary" sx={{ mr: 1 }}>
                {isMaxTier 
                  ? "최고 티어입니다!"
                  : expToNextTier !== null ? `다음 티어까지: ${expToNextTier} EXP` : ""}
              </Typography>
            )}
        </Box>
      </Box>
    </Paper>
  );
};

export default UserTierRank;