import React from 'react';
import styled from 'styled-components';
import NavigationBar from '../../components/NavigationBar';
import PasswordConfirmation from '../../components/PasswordConfirmation';

const AppContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  width: 100vw;
  overflow-x: hidden;
`;

export default function PasswordConfirmationPage() {
  return (
    <AppContainer>
      <PasswordConfirmation></PasswordConfirmation>
      <NavigationBar></NavigationBar>
    </AppContainer>
  );
}
