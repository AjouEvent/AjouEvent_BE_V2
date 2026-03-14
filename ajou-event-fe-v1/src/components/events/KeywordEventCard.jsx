import styled from 'styled-components';
import EmptyStarIcon from '../icons/EmptyStarIcon';
import FilledStarIcon from '../icons/FilledStarIcon';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import requestWithAccessToken from '../../services/jwt/requestWithAccessToken';
import Swal from 'sweetalert2';

const CardContainer = styled.div`
  display: flex;
  align-items: flex-start;
  gap: 16px;
  align-self: stretch;
  width: calc(100vw - 40px);
  height: 144px;
  text-decoration: none;
  margin-top: 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  cursor: pointer;
`;

const Image = styled.img`
  border-radius: 20px;
  overflow: hidden;
  width: 120px;
  height: 120px;
`;

const ImageWapper = styled.div`
  width: 25px;
  height: 25px;
  object-fit: cover;
  cursor: pointer;
`;

const CardImageWapper = styled.div`
  object-fit: cover;
  width: 120px;
  height: 120px;
`;

const DetailsContainer = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  flex-grow: 1;
`;

const TitleContainer = styled.div`
  display: flex;
  width: 100%;
  flex-direction: column;
  gap: 6px;
`;

const KeywordContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 4px;
  height: 30px;
`;

const BellIcon = styled.img`
  width: 18px;
  height: 18px;
`;

const KeywordText = styled.div`
  font-size: 16px;
  font-weight: bold;
  color: #333;
`;

const TitleText = styled.div`
  display: flex;
  width: 100%;
  height: 40px;
  color: #000;
  font-family: 'Pretendard Variable';
  font-size: 16px;
  font-style: normal;
  font-weight: 600;
  line-height: 120%;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: keep-all;
  overflow: hidden;
  text-overflow: ellipsis;
`;

const Subject = styled.div`
  width: fit-content;
  padding: 3px 4px;
  height: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 4px;
  background: rgba(84, 84, 84, 0.08);
  font-size: 14px;
  color: rgba(84, 84, 84);
  font-weight: bold;
  font-family: 'Pretendard Variable';
`;

const Stats = styled.div`
  align-self: start;
  display: flex;
  color: #c2c8d1;
  white-space: nowrap;
  text-align: center;
`;

const StatItem = styled.div`
  display: flex;
  padding-right: 20px;
  gap: 4px;
`;

const StatIcon = styled.img`
  aspect-ratio: 1;
  object-fit: auto;
  object-position: center;
  width: 14px;
`;

const StatValue = styled.span`
  font-family: 'Pretendard Variable';
`;

const TagContaioner = styled.div`
  display: flex;
  width: 100%;
  justify-content: space-between;
  align-items: end;
`;

const LikeContainer = styled.div`
  display: flex;
  width: 30px;
  justify-content: space-between;
  align-items: center;
`;

function Stat({ iconSrc, value, altText }) {
  return (
    <StatItem>
      <StatIcon src={iconSrc} alt={altText} loading="lazy" />
      <StatValue>{value}</StatValue>
    </StatItem>
  );
}

const KeywordEventCard = ({
  id,
  title,
  subject,
  content,
  imgUrl,
  likesCount,
  viewCount,
  star,
  keyword, // 추가된 키워드 prop
}) => {
  const [cardStar, setCardStar] = useState(star);
  const [likes, setLikes] = useState(likesCount);
  const navigate = useNavigate();

  // HTTP 이미지 URL을 HTTPS로 변환
  const getSafeImageUrl = (url) => {
    if (url.startsWith('http://')) {
      return `https://images.weserv.nl/?url=${encodeURIComponent(url)}`;
    }
    return url; // HTTPS URL은 그대로 반환
  };

  const handleStarClick = async (e) => {
    e.stopPropagation();

    try {
      if (cardStar) {
        await requestWithAccessToken(
          'delete',
          `${process.env.REACT_APP_BE_URL}/api/event/like/${id}`,
        );
        setCardStar(!cardStar);
        setLikes(likes - 1);
      } else {
        await requestWithAccessToken(
          'post',
          `${process.env.REACT_APP_BE_URL}/api/event/like/${id}`,
        );
        setCardStar(!cardStar);
        setLikes(likes + 1);
      }
    } catch (error) {
      console.error('Error toggling like:', error);
      Swal.fire({
        icon: 'error',
        title: '좋아요 에러',
        text: '로그인이 필요한 기능입니다.',
      });
    }
  };

  const handleCardClick = () => {
    navigate(`/event/${id}`);
  };

  return (
    <CardContainer onClick={handleCardClick}>
      <CardImageWapper>
        <Image src={getSafeImageUrl(imgUrl)} alt={title} loading="lazy" />
      </CardImageWapper>
      <DetailsContainer>
        <TitleContainer>
          <div>
            <KeywordContainer>
              <BellIcon
                src={`${process.env.PUBLIC_URL}/icons/alarm_filled.svg`}
                alt="Notification Bell"
              />
              <KeywordText>{keyword}</KeywordText>
            </KeywordContainer>
            <TitleText>{title}</TitleText>
            <Subject>{subject}</Subject>
          </div>
        </TitleContainer>
        <TagContaioner>
          <Stats>
            <Stat
              iconSrc="https://cdn.builder.io/api/v1/image/assets/TEMP/62c7bb15f5fd13739601caff1be349795102bd00b8ccfe603cd2e43498657c46?apiKey=75213697ab8e4fbfb70997e546d69efb&"
              value={viewCount}
              altText="Statistic icon 1"
            />
            <Stat
              onClick={handleStarClick}
              iconSrc="https://cdn.builder.io/api/v1/image/assets/TEMP/52d95bd6c4badc487be46d013f44cd23b9800d5d1e753fb3a364bcb97b18044f?apiKey=75213697ab8e4fbfb70997e546d69efb&"
              value={likes}
              altText="Statistic icon 2"
            />
          </Stats>
          <LikeContainer>
            <ImageWapper onClick={handleStarClick}>
              {cardStar ? (
                <FilledStarIcon></FilledStarIcon>
              ) : (
                <EmptyStarIcon></EmptyStarIcon>
              )}
            </ImageWapper>
          </LikeContainer>
        </TagContaioner>
      </DetailsContainer>
    </CardContainer>
  );
};

export default KeywordEventCard;
