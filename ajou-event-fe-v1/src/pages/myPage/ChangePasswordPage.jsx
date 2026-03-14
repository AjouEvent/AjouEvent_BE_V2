import React from 'react';
import styled from 'styled-components';
import FindPassword from '../../components/ChangePassword';
import NavigationBar from '../../components/NavigationBar';

const AppContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: white;
  height: 100vh;
  overflow-y: hidden;
  width: 100vw;
  overflow-x: hidden;
`;

export default function ChangePasswordPage() {
  return (
    <AppContainer>
      <FindPassword></FindPassword>
      <NavigationBar></NavigationBar>
    </AppContainer>
  );
}
